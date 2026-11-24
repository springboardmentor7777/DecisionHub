package com.decisionhub.service;

import com.decisionhub.dto.PollDtos.*;
import com.decisionhub.entity.*;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.exception.ForbiddenException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PollService {
    private final PollRepository pollRepository;
    private final PollOptionRepository optionRepository;
    private final PollVoteRepository voteRepository;
    private final DecisionBoardService boardService;
    private final UserRepository userRepository;

    @Transactional
    public PollResponse create(String email, Long boardId, CreatePollRequest request) {
        DecisionBoard board = boardService.ownedBoard(email, boardId);
        List<String> labels = request.options() == null ? List.of() : request.options();
        if (request.type() == PollType.RATING) {
            if (!labels.isEmpty()) {
                throw new BadRequestException("Rating polls do not use options");
            }
        } else if (labels.size() < 2) {
            throw new BadRequestException("Choice polls require at least two options");
        }

        Poll poll = new Poll();
        poll.setBoard(board);
        poll.setCreatedBy(board.getCreatedBy());
        poll.setQuestion(request.question().trim());
        poll.setType(request.type());
        poll.setAnonymous(request.anonymous());
        for (String label : labels) {
            PollOption option = new PollOption();
            option.setPoll(poll);
            option.setLabel(label.trim());
            poll.getOptions().add(option);
        }
        return PollResponse.from(pollRepository.save(poll));
    }

    @Transactional(readOnly = true)
    public List<PollResponse> listByBoard(String email, Long boardId) {
        boardService.accessibleBoard(email, boardId);
        return pollRepository.findAllByBoardIdOrderByCreatedAtDesc(boardId).stream()
                .map(PollResponse::from)
                .toList();
    }

    @Transactional
    public PollResponse get(String email, Long pollId) {
        return PollResponse.from(accessiblePoll(email, pollId));
    }

    @Transactional
    public void castVote(String email, Long pollId, CastVoteRequest request) {
        Poll poll = accessiblePoll(email, pollId);
        if (!poll.isActive()) {
            throw new BadRequestException("This poll is closed");
        }
        User voter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (voteRepository.existsByPollIdAndVoterId(pollId, voter.getId())) {
            throw new BadRequestException("You have already voted in this poll");
        }

        List<Long> optionIds = request.optionIds() == null ? List.of() : request.optionIds();
        if (poll.getType() == PollType.RATING) {
            if (request.rating() == null || !optionIds.isEmpty()) {
                throw new BadRequestException("Rating polls require a rating from 1 to 5 only");
            }
        } else {
            if (request.rating() != null || optionIds.isEmpty()) {
                throw new BadRequestException("Choice polls require one or more option IDs only");
            }
            if (poll.getType() == PollType.SINGLE_CHOICE && optionIds.size() != 1) {
                throw new BadRequestException("Select exactly one option");
            }
            if (new HashSet<>(optionIds).size() != optionIds.size()) {
                throw new BadRequestException("An option can only be selected once");
            }
        }

        PollVote vote = new PollVote();
        vote.setPoll(poll);
        vote.setVoter(voter);
        vote.setRating(request.rating());
        for (Long optionId : optionIds) {
            PollOption option = optionRepository.findByIdAndPollId(optionId, pollId)
                    .orElseThrow(() -> new BadRequestException("Selected option does not belong to this poll"));
            VoteSelection selection = new VoteSelection();
            selection.setVote(vote);
            selection.setOption(option);
            vote.getSelections().add(selection);
        }
        voteRepository.save(vote);
    }

    @Transactional(readOnly = true)
    public PollResults results(String email, Long pollId) {
        Poll poll = accessiblePoll(email, pollId);
        List<PollVote> votes = voteRepository.findAllByPollId(pollId);
        long totalVotes = votes.size();
        BigDecimal ratingTotal = BigDecimal.ZERO;
        long ratingCount = 0;
        List<PollOptionResult> results = new ArrayList<>();
        for (PollOption option : poll.getOptions()) {
            long count = votes.stream()
                    .flatMap(vote -> vote.getSelections().stream())
                    .filter(selection -> selection.getOption().getId().equals(option.getId()))
                    .count();
            results.add(new PollOptionResult(option.getId(), option.getLabel(), count,
                    percentage(count, totalVotes)));
        }
        for (PollVote vote : votes) {
            if (vote.getRating() != null) {
                ratingTotal = ratingTotal.add(BigDecimal.valueOf(vote.getRating()));
                ratingCount++;
            }
        }
        BigDecimal averageRating = ratingCount == 0 ? null
                : ratingTotal.divide(BigDecimal.valueOf(ratingCount), 2, RoundingMode.HALF_UP);
        return new PollResults(pollId, totalVotes, averageRating, results);
    }

    @Transactional
    public PollResponse close(String email, Long pollId) {
        Poll poll = accessiblePoll(email, pollId);
        if (!poll.getCreatedBy().getEmail().equals(email)) {
            throw new ForbiddenException("Only the poll owner can close this poll");
        }
        poll.setActive(false);
        return PollResponse.from(pollRepository.save(poll));
    }

    private Poll accessiblePoll(String email, Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));
        boardService.accessibleBoard(email, poll.getBoard().getId());
        return poll;
    }

    private static BigDecimal percentage(long count, long total) {
        return total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}
