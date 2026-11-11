package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.*;
import com.decisionhub.entity.*;
import com.decisionhub.repository.BoardCommentRepository;
import com.decisionhub.repository.BoardVoteRepository;
import com.decisionhub.repository.DecisionBoardRepository;
import com.decisionhub.repository.UserRepository;
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<BoardResponse> publicBoards() {
        return boards.findAllByVisibilityOrderByCreatedAtDesc(BoardVisibility.PUBLIC).stream()
                .map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> myBoards(String email) {
        AppUser user = user(email);
        return boards.findAllByOwner_IdOrderByCreatedAtDesc(user.getId()).stream().map(this::response).toList();
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
        board.update(request.title().trim(), request.description(), request.category().trim(),
                request.visibility(), request.pollType());
        return response(board);
    }

    @Transactional
    public void delete(Long id, String email) {
        DecisionBoard board = ownedBoard(id, email);
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

    private BoardResponse response(DecisionBoard board) {
        List<OptionResponse> options = board.getOptions().stream()
                .map(option -> new OptionResponse(option.getId(), option.getName(), option.getDescription(),
                        option.getPros(), option.getCons(), option.getScore())).toList();
        return new BoardResponse(board.getId(), board.getTitle(), board.getDescription(), board.getCategory(),
                board.getVisibility(), board.getPollType(), board.getOwner().getId(),
                board.getOwner().getDisplayName(), board.getCreatedAt(), options);
    }
}
