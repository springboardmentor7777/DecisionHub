package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.*;
import com.decisionhub.entity.*;
import com.decisionhub.repository.BoardCommentRepository;
import com.decisionhub.repository.BoardVoteRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {
    private final DecisionBoardRepository boards;
    private final BoardVoteRepository votes;
    private final BoardCommentRepository comments;
    private final UserRepository users;

    public BoardService(DecisionBoardRepository boards, BoardVoteRepository votes,
                        BoardCommentRepository comments, UserRepository users) {
        this.boards = boards;
        this.votes = votes;
        this.comments = comments;
        this.users = users;
    }

    /** Public boards, newest first. All filters are optional and can be combined. */
    @Transactional(readOnly = true)
    public List<BoardResponse> publicBoards(String category, String query, PollType pollType) {
        return boards.findAllByVisibilityOrderByCreatedAtDesc(BoardVisibility.PUBLIC).stream()
                .filter(board -> matches(board, category, query, pollType))
                .map(this::response).toList();
    }

    /** The caller's own boards, newest first. All filters are optional and can be combined. */
    @Transactional(readOnly = true)
    public List<BoardResponse> myBoards(String email, String category, String query,
                                        PollType pollType, BoardVisibility visibility) {
        AppUser user = user(email);
        return boards.findAllByOwner_IdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(board -> visibility == null || board.getVisibility() == visibility)
                .filter(board -> matches(board, category, query, pollType))
                .map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public BoardResponse get(Long id, String email) {
        return response(accessibleBoard(id, email));
    }

    @Transactional
    public BoardResponse create(String email, CreateBoardRequest request) {
        AppUser owner = user(email);
        DecisionBoard board = new DecisionBoard(request.title().trim(), request.description(),
                request.category().trim(), request.visibility(), request.pollType(), owner);
        for (OptionRequest option : request.options()) {
            board.addOption(new DecisionOption(option.name().trim(), option.description(),
                    option.pros(), option.cons(), option.score()));
        }
        return response(boards.save(board));
    }

    @Transactional
    public BoardResponse update(Long id, String email, UpdateBoardRequest request) {
        DecisionBoard board = ownedBoard(id, email);
        if (request.pollType() != board.getPollType() && votes.countByBoard_Id(id) > 0) {
            throw new ConflictException("The poll type cannot be changed after voting has started");
        }
        board.update(request.title().trim(), request.description(), request.category().trim(),
                request.visibility(), request.pollType());
        return response(board);
    }

    @Transactional
    public void delete(Long id, String email) {
        removeBoard(ownedBoard(id, email));
    }

    /** Deletes a board with its comments and votes. The caller must already have checked permissions. */
    @Transactional
    public void removeBoard(DecisionBoard board) {
        Long id = board.getId();
        for (BoardComment comment : comments.findAllByBoard_IdOrderByCreatedAtDesc(id)) {
            comments.delete(comment);
            comments.flush();
        }
        votes.deleteAllByBoard_Id(id);
        boards.delete(board);
    }

    @Transactional
    public BoardResponse addOption(Long boardId, String email, OptionRequest request) {
        DecisionBoard board = ownedBoard(boardId, email);
        if (board.getOptions().size() >= 20) {
            throw new IllegalArgumentException("A board can have at most 20 options");
        }
        board.addOption(new DecisionOption(request.name().trim(), request.description(),
                request.pros(), request.cons(), request.score()));
        return response(board);
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> options(Long boardId, String email) {
        return accessibleBoard(boardId, email).getOptions().stream().map(this::optionResponse).toList();
    }

    @Transactional
    public BoardResponse updateOption(Long boardId, Long optionId, String email, OptionRequest request) {
        DecisionBoard board = ownedBoard(boardId, email);
        DecisionOption option = findOption(board, optionId);
        option.update(request.name().trim(), request.description(), request.pros(), request.cons(),
                request.score());
        return response(board);
    }

    @Transactional
    public BoardResponse deleteOption(Long boardId, Long optionId, String email) {
        DecisionBoard board = ownedBoard(boardId, email);
        DecisionOption option = findOption(board, optionId);
        if (board.getOptions().size() <= 2) {
            throw new ConflictException("A board needs at least 2 options");
        }
        if (votes.countByOption_Id(optionId) > 0) {
            throw new ConflictException("This option already has votes and cannot be deleted");
        }
        board.getOptions().remove(option); // orphan removal deletes the option
        return response(board);
    }

    @Transactional(readOnly = true)
    public DecisionBoard accessibleBoard(Long id, String email) {
        DecisionBoard board = boards.findWithOptionsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision board not found"));
        if (board.getVisibility() == BoardVisibility.PRIVATE && !board.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("This decision board is private");
        }
        return board;
    }

    private DecisionBoard ownedBoard(Long id, String email) {
        DecisionBoard board = boards.findWithOptionsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision board not found"));
        if (!board.getOwner().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("Only the board owner can modify this board");
        }
        return board;
    }

    private AppUser user(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private DecisionOption findOption(DecisionBoard board, Long optionId) {
        return board.getOptions().stream()
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Option not found"));
    }

    private static boolean matches(DecisionBoard board, String category, String query, PollType pollType) {
        if (category != null && !category.isBlank() && !board.getCategory().equalsIgnoreCase(category.trim())) {
            return false;
        }
        if (pollType != null && board.getPollType() != pollType) {
            return false;
        }
        if (query != null && !query.isBlank()) {
            String needle = query.trim().toLowerCase(Locale.ROOT);
            boolean found = contains(board.getTitle(), needle)
                    || contains(board.getDescription(), needle)
                    || board.getOptions().stream().anyMatch(option -> contains(option.getName(), needle));
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private static boolean contains(String text, String needle) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(needle);
    }

    private OptionResponse optionResponse(DecisionOption option) {
        return new OptionResponse(option.getId(), option.getName(), option.getDescription(),
                option.getPros(), option.getCons(), option.getScore());
    }

    private BoardResponse response(DecisionBoard board) {
        List<OptionResponse> options = board.getOptions().stream().map(this::optionResponse).toList();
        return new BoardResponse(board.getId(), board.getTitle(), board.getDescription(), board.getCategory(),
                board.getVisibility(), board.getPollType(), board.getOwner().getId(),
                board.getOwner().getDisplayName(), board.getCreatedAt(), options);
    }
}
