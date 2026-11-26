import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import boardsApi from '../api/boards';
import useAuth from '../hooks/useAuth';
import BoardForm from '../components/boards/BoardForm';
import Card from '../components/common/Card';
import Alert from '../components/common/Alert';
import Spinner from '../components/common/Spinner';

const BoardEditPage = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [board, setBoard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [forbidden, setForbidden] = useState(false);

  useEffect(() => {
    let active = true;
    boardsApi.get(id).then((data) => {
      if (!active) return;
      setBoard(data);
      setForbidden(String(data.ownerId) !== String(user?.id));
    }).catch((loadError) => {
      if (active) setError(loadError.message);
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [id, user?.id]);

  const updateBoard = async (payload) => {
    const updated = await boardsApi.update(id, payload);
    navigate(`/boards/${updated.id}`);
  };

  if (loading) return <div className="flex justify-center py-20"><Spinner size="lg" className="text-brand-700" /></div>;
  if (error || forbidden || !board) {
    return <div className="space-y-4">
      <Link to={`/boards/${id}`} className="text-sm font-semibold text-brand-800">Back to board</Link>
      <Alert type="error">{error || (forbidden ? 'Only the board owner can edit this decision.' : 'Board could not be found.')}</Alert>
    </div>;
  }

  return (
    <Card>
      <Link to={`/boards/${id}`} className="text-sm font-semibold text-brand-800">← Back to board</Link>
      <h1 className="mb-5 mt-3 font-display text-2xl font-extrabold">Edit decision</h1>
      <BoardForm mode="edit" initialValues={board} onSubmit={updateBoard} />
    </Card>
  );
};

export default BoardEditPage;
