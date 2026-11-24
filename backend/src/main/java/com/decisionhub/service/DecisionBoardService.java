package com.decisionhub.service;

import com.decisionhub.dto.DecisionDtos.*;
import com.decisionhub.entity.*;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DecisionBoardService {
    private final DecisionBoardRepository boardRepository;
    private final DecisionOptionRepository optionRepository;
    private final ComparisonFactorRepository factorRepository;
    private final UserRepository userRepository;

    @Transactional
    public BoardResponse create(String email, CreateBoardRequest request) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        DecisionBoard board = new DecisionBoard();
        board.setTitle(request.title().trim());
        board.setDescription(request.description());
        board.setCategory(CategoryCatalog.normalize(request.category()));
        board.setVisibility(request.visibility());
        board.setCreatedBy(owner);
        if (request.options() != null) {
            request.options().forEach(input -> board.getOptions().add(toOption(input, board)));
        }
        return BoardResponse.from(boardRepository.save(board));
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> list(String email, String category) {
        String normalizedCategory = CategoryCatalog.normalize(category);
        return boardRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(board -> board.getVisibility() == BoardVisibility.PUBLIC
                        || board.getCreatedBy().getEmail().equals(email))
                .filter(board -> normalizedCategory == null
                        || normalizedCategory.equalsIgnoreCase(board.getCategory()))
                .map(BoardResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoardResponse get(String email, Long id) {
        return BoardResponse.from(accessibleBoard(email, id));
    }

    @Transactional
    public BoardResponse update(String email, Long id, UpdateBoardRequest request) {
        DecisionBoard board = ownedBoard(email, id);
        board.setTitle(request.title().trim());
        board.setDescription(request.description());
        board.setCategory(CategoryCatalog.normalize(request.category()));
        board.setVisibility(request.visibility());
        board.setStatus(request.status().trim().toUpperCase());
        return BoardResponse.from(boardRepository.save(board));
    }

    @Transactional
    public void delete(String email, Long id) {
        boardRepository.delete(ownedBoard(email, id));
    }

    @Transactional
    public OptionResponse addOption(String email, Long boardId, OptionRequest request) {
        DecisionBoard board = ownedBoard(email, boardId);
        return OptionResponse.from(optionRepository.save(toOption(request, board)));
    }

    @Transactional
    public OptionResponse updateOption(String email, Long boardId, Long optionId,
                                      OptionRequest request) {
        DecisionBoard board = ownedBoard(email, boardId);
        DecisionOption option = optionRepository.findByIdAndBoardId(optionId, boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
        applyOption(option, request);
        return OptionResponse.from(optionRepository.save(option));
    }

    @Transactional
    public void deleteOption(String email, Long boardId, Long optionId) {
        ownedBoard(email, boardId);
        DecisionOption option = optionRepository.findByIdAndBoardId(optionId, boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
        optionRepository.delete(option);
    }

    @Transactional
    public List<FactorResponse> replaceFactors(String email, Long boardId,
                                               List<FactorRequest> requests) {
        DecisionBoard board = ownedBoard(email, boardId);
        Set<String> names = new HashSet<>();
        for (FactorRequest request : requests) {
            if (!names.add(request.name().trim().toLowerCase())) {
                throw new BadRequestException("Comparison factor names must be unique");
            }
        }
        board.getOptions().forEach(option -> option.getScores().clear());
        board.getFactors().clear();
        for (FactorRequest request : requests) {
            ComparisonFactor factor = new ComparisonFactor();
            factor.setBoard(board);
            factor.setName(request.name().trim());
            factor.setWeight(request.weight());
            board.getFactors().add(factor);
        }
        boardRepository.save(board);
        return board.getFactors().stream()
                .map(factor -> new FactorResponse(factor.getId(), factor.getName(), factor.getWeight()))
                .toList();
    }

    @Transactional
    public List<FactorResponse> listFactors(String email, Long boardId) {
        accessibleBoard(email, boardId);
        return factorRepository.findAllByBoardIdOrderById(boardId).stream()
                .map(factor -> new FactorResponse(factor.getId(), factor.getName(), factor.getWeight()))
                .toList();
    }

    @Transactional
    public void setScores(String email, Long boardId, Long optionId, ScoreRequest request) {
        DecisionBoard board = ownedBoard(email, boardId);
        DecisionOption option = optionRepository.findByIdAndBoardId(optionId, boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
        List<ComparisonFactor> factors = factorRepository.findAllByBoardIdOrderById(boardId);
        Set<Long> factorIds = new HashSet<>();
        for (ScoreInput input : request.scores()) {
            if (!factorIds.add(input.factorId())) {
                throw new BadRequestException("A comparison factor can only be scored once");
            }
            if (factors.stream().noneMatch(factor -> factor.getId().equals(input.factorId()))) {
                throw new BadRequestException("All scores must use factors from this decision");
            }
        }

        option.getScores().clear();
        for (ScoreInput input : request.scores()) {
            ComparisonFactor factor = factors.stream()
                    .filter(candidate -> candidate.getId().equals(input.factorId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Comparison factor not found"));
            OptionFactorScore score = new OptionFactorScore();
            score.setOption(option);
            score.setFactor(factor);
            score.setScore(input.score());
            option.getScores().add(score);
        }
        optionRepository.save(option);
    }

    @Transactional(readOnly = true)
    public ComparisonResponse compare(String email, Long boardId) {
        DecisionBoard board = accessibleBoard(email, boardId);
        List<ComparisonFactor> factors = factorRepository.findAllByBoardIdOrderById(boardId);
        List<FactorResponse> factorResponses = factors.stream()
                .map(factor -> new FactorResponse(factor.getId(), factor.getName(), factor.getWeight()))
                .toList();
        List<RankedOption> ranking = new ArrayList<>();
        for (DecisionOption option : board.getOptions()) {
            ComparisonScoring.Result result = ComparisonScoring.calculate(factors, option.getScores());
            ranking.add(new RankedOption(option.getId(), option.getTitle(), result.value(),
                    result.complete()));
        }
        ranking.sort((left, right) -> right.weightedScore().compareTo(left.weightedScore()));
        return new ComparisonResponse(boardId, factorResponses, ranking);
    }

    @Transactional(readOnly = true)
    public DecisionBoard accessibleBoard(String email, Long id) {
        DecisionBoard board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision board not found"));
        if (board.getVisibility() == BoardVisibility.PRIVATE
                && !board.getCreatedBy().getEmail().equals(email)) {
            throw new ResourceNotFoundException("Decision board not found");
        }
        return board;
    }

    @Transactional(readOnly = true)
    public DecisionBoard ownedBoard(String email, Long id) {
        DecisionBoard board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision board not found"));
        if (!board.getCreatedBy().getEmail().equals(email)) {
            throw new ForbiddenException("Only the board owner can modify this decision");
        }
        return board;
    }

    private static DecisionOption toOption(OptionRequest request, DecisionBoard board) {
        DecisionOption option = new DecisionOption();
        option.setBoard(board);
        applyOption(option, request);
        return option;
    }

    private static void applyOption(DecisionOption option, OptionRequest request) {
        option.setTitle(request.title().trim());
        option.setDescription(request.description());
        option.setPros(request.pros());
        option.setCons(request.cons());
        option.setEstimatedCost(request.estimatedCost());
        option.setBenefits(request.benefits());
        option.setRisks(request.risks());
    }

}
