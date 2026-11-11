package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.CommentRequest;
import com.decisionhub.dto.ApiDtos.CommentResponse;
import com.decisionhub.entity.AppUser;
import com.decisionhub.entity.BoardComment;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.UserNotification;
import com.decisionhub.repository.BoardCommentRepository;
import com.decisionhub.repository.UserNotificationRepository;
import com.decisionhub.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final BoardCommentRepository comments;
    private final UserRepository users;
    private final BoardService boards;
    private final UserNotificationRepository notifications;

    public CommentService(BoardCommentRepository comments, UserRepository users, BoardService boards,
                          UserNotificationRepository notifications) {
        this.comments = comments;
        this.users = users;
        this.boards = boards;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long boardId, String email) {
        boards.accessibleBoard(boardId, email);
        return comments.findAllByBoard_IdAndParentIsNullOrderByCreatedAtAsc(boardId).stream()
                .map(this::response).toList();
    }

    @Transactional
    public CommentResponse add(Long boardId, String email, CommentRequest request) {
        if (email == null) {
            throw new ForbiddenException("Sign in to join the discussion");
        }
        DecisionBoard board = boards.accessibleBoard(boardId, email);
        AppUser author = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        BoardComment parent = null;
        if (request.parentId() != null) {
            parent = comments.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            if (!parent.getBoard().getId().equals(boardId)) {
                throw new IllegalArgumentException("Parent comment belongs to another board");
            }
        }
        BoardComment saved = comments.save(new BoardComment(board, author, parent, request.content().trim()));
        if (!board.getOwner().getId().equals(author.getId())) {
            notifications.save(new UserNotification(board.getOwner(),
                    author.getDisplayName() + " commented on your decision",
                    "/boards/" + boardId + "#comments"));
        }
        return response(saved);
    }

    private CommentResponse response(BoardComment comment) {
        return new CommentResponse(comment.getId(), comment.getBoard().getId(),
                comment.getParent() == null ? null : comment.getParent().getId(),
                comment.getAuthor().getId(), comment.getAuthor().getDisplayName(),
                comment.getContent(), comment.getCreatedAt(),
                comment.getReplies().stream().map(this::response).toList());
    }
}
