package com.decisionhub.service;

import com.decisionhub.dto.PollDtos.CastVoteRequest;
import com.decisionhub.entity.DecisionBoard;
import com.decisionhub.entity.Poll;
import com.decisionhub.entity.PollType;
import com.decisionhub.entity.User;
import com.decisionhub.exception.BadRequestException;
import com.decisionhub.repository.PollOptionRepository;
import com.decisionhub.repository.PollRepository;
import com.decisionhub.repository.PollVoteRepository;
import com.decisionhub.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PollVotingTest {
    @Test
    void rejectsDuplicateVotesBeforeSavingAnotherVote() {
        PollRepository polls = mock(PollRepository.class);
        PollVoteRepository votes = mock(PollVoteRepository.class);
        UserRepository users = mock(UserRepository.class);
        DecisionBoardService boards = mock(DecisionBoardService.class);

        User user = new User();
        user.setId(4L);
        user.setEmail("voter@example.com");
        DecisionBoard board = new DecisionBoard();
        board.setId(8L);
        Poll poll = new Poll();
        poll.setId(2L);
        poll.setBoard(board);
        poll.setType(PollType.SINGLE_CHOICE);
        poll.setActive(true);

        when(polls.findById(2L)).thenReturn(Optional.of(poll));
        when(boards.accessibleBoard("voter@example.com", 8L)).thenReturn(board);
        when(users.findByEmail("voter@example.com")).thenReturn(Optional.of(user));
        when(votes.existsByPollIdAndVoterId(2L, 4L)).thenReturn(true);

        PollService service = new PollService(
                polls, mock(PollOptionRepository.class), votes, boards, users);

        assertThrows(BadRequestException.class,
                () -> service.castVote("voter@example.com", 2L,
                        new CastVoteRequest(List.of(10L), null)));
    }
}
