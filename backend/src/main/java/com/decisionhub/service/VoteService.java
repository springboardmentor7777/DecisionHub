package com.decisionhub.service;

import com.decisionhub.dto.ApiDtos.VoteRequest;
import com.decisionhub.dto.ApiDtos.VoteResponse;
import com.decisionhub.entity.*;
import com.decisionhub.repository.BoardVoteRepository;
import com.decisionhub.repository.DecisionOptionRepository;
import com.decisionhub.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {
    private final BoardService boardService;
    private final BoardVoteRepository votes;
    private final DecisionOptionRepository options;
    private final UserRepository users;

    public VoteService(BoardService boardService, BoardVoteRepository votes,
                       DecisionOptionRepository options, UserRepository users) {
        this.boardService = boardService;
        this.votes = votes;
        this.options = options;
        this.users = users;
    }

    @Transactional
    public VoteResponse vote(Long boardId, String email, VoteRequest request) {
        DecisionBoard board = boardService.accessibleBoard(boardId, email);
        DecisionOption option = options.findByIdAndBoard_Id(request.optionId(), boardId)
                .orElseThrow(() -> new IllegalArgumentException("The selected option does not belong to this board"));

        AppUser user = null;
        if (!request.anonymous() && email != null) {
            user = users.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } else if (!request.anonymous()) {
            throw new ForbiddenException("Sign in to cast a non-anonymous vote");
        }

        if (board.getPollType() == PollType.RATING) {
            if (request.rating() == null) {
                throw new IllegalArgumentException("Rating polls require a rating from 1 to 5");
            }
        } else if (request.rating() != null) {
            throw new IllegalArgumentException("Ratings can only be submitted to rating polls");
        }

        if (user != null) {
            if (board.getPollType() != PollType.MULTIPLE_CHOICE
                    && votes.findByBoard_IdAndUser_Id(boardId, user.getId()).isPresent()) {
                throw new IllegalArgumentException("You have already voted on this poll");
            }
            if (board.getPollType() == PollType.MULTIPLE_CHOICE
                    && votes.existsByBoard_IdAndUser_IdAndOption_Id(boardId, user.getId(), option.getId())) {
                throw new IllegalArgumentException("You have already selected this option");
            }
        }

        votes.save(new BoardVote(board, option, user, request.anonymous(), request.rating()));
        return results(board);
    }

    @Transactional(readOnly = true)
    public VoteResponse results(Long boardId, String email) {
        return results(boardService.accessibleBoard(boardId, email));
    }

    private VoteResponse results(DecisionBoard board) {
        Map<Long, Long> counts = new LinkedHashMap<>();
        for (Object[] row : votes.countVotesByOption(board.getId())) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        board.getOptions().forEach(option -> counts.putIfAbsent(option.getId(), 0L));
        return new VoteResponse(board.getId(), votes.countByBoard_Id(board.getId()), counts);
    }
}
