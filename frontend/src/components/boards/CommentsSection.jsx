import React, { useCallback, useEffect, useState } from 'react';
import commentsApi from '../../api/comments';
import Alert from '../common/Alert';
import Button from '../common/Button';
import Spinner from '../common/Spinner';

const CommentItem = ({ comment, onReply }) => {
  const [replying, setReplying] = useState(false);
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const submitReply = async (event) => {
    event.preventDefault();
    setError('');
    if (!content.trim()) {
      setError('Write a reply before submitting.');
      return;
    }
    setSubmitting(true);
    try {
      await onReply(content.trim(), comment.id);
      setContent('');
      setReplying(false);
    } catch (replyError) {
      setError(replyError.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <li className="border-l-2 border-brand-100 pl-4">
      <p className="text-sm font-semibold">{comment.authorName || 'DecisionHub member'}</p>
      <p className="mt-1 whitespace-pre-wrap text-sm leading-relaxed text-ink/75">{comment.content}</p>
      <div className="mt-2 flex items-center gap-3 text-xs text-ink/50">
        <time dateTime={comment.createdAt}>{new Date(comment.createdAt).toLocaleString()}</time>
        <button type="button" className="font-semibold text-brand-800" onClick={() => setReplying((value) => !value)}>
          {replying ? 'Cancel reply' : 'Reply'}
        </button>
      </div>
      {error && <div className="mt-3"><Alert type="error">{error}</Alert></div>}
      {replying && <form onSubmit={submitReply} className="mt-3 space-y-2">
        <label htmlFor={`reply-${comment.id}`} className="sr-only">Reply to {comment.authorName || 'comment'}</label>
        <textarea id={`reply-${comment.id}`} value={content} onChange={(event) => setContent(event.target.value)}
          maxLength={3000} rows={2} className="w-full rounded-lg border border-brand-900/20 bg-white px-3 py-2 text-sm" required />
        <Button type="submit" size="sm" loading={submitting}>Post reply</Button>
      </form>}
      {comment.replies?.length > 0 && (
        <ul className="mt-4 space-y-4">
          {comment.replies.map((reply) => <CommentItem key={reply.id} comment={reply} onReply={onReply} />)}
        </ul>
      )}
    </li>
  );
};

const CommentsSection = ({ boardId }) => {
  const [comments, setComments] = useState([]);
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const loadComments = useCallback(async () => {
    setError('');
    setComments(await commentsApi.list(boardId));
  }, [boardId]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    commentsApi.list(boardId).then((data) => {
      if (active) setComments(data);
    }).catch((loadError) => {
      if (active) setError(loadError.message);
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [boardId]);

  const postComment = async (text, parentId) => {
    await commentsApi.create(boardId, { content: text, parentId });
    await loadComments();
  };

  const submitComment = async (event) => {
    event.preventDefault();
    setError('');
    if (!content.trim()) {
      setError('Write a comment before submitting.');
      return;
    }
    setSubmitting(true);
    try {
      await postComment(content.trim());
      setContent('');
    } catch (postError) {
      setError(postError.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section id="comments" className="rounded-xl border border-brand-900/10 bg-white p-5 sm:p-6">
      <h2 className="font-display text-xl font-bold">Discussion</h2>
      <form onSubmit={submitComment} className="mt-4 space-y-3">
        <label htmlFor={`comment-${boardId}`} className="block text-sm font-semibold">Add a comment</label>
        <textarea id={`comment-${boardId}`} value={content} onChange={(event) => setContent(event.target.value)}
          maxLength={3000} rows={3} className="w-full rounded-lg border border-brand-900/20 bg-white px-3 py-2 text-sm" required />
        <Button type="submit" loading={submitting}>Post comment</Button>
      </form>
      {error && <div className="mt-4"><Alert type="error">{error}</Alert></div>}
      {loading ? <div className="flex justify-center py-6"><Spinner className="text-brand-700" /></div>
        : comments.length ? (
          <ul className="mt-6 space-y-5">
            {comments.map((comment) => <CommentItem key={comment.id} comment={comment} onReply={postComment} />)}
          </ul>
        ) : <p className="mt-6 text-sm text-ink/55">No comments yet. Start the discussion.</p>}
    </section>
  );
};

export default CommentsSection;
