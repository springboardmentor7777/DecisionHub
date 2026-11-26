import React, { useEffect, useMemo, useState } from 'react';
import { Plus } from 'lucide-react';
import communitiesApi, { COMMUNITY_CATEGORIES } from '../api/communities';
import useAuth from '../hooks/useAuth';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import Input from '../components/common/Input';
import Alert from '../components/common/Alert';
import Spinner from '../components/common/Spinner';

const labelCategory = (category) =>
  category.charAt(0) + category.slice(1).toLowerCase();

const CommunitiesPage = () => {
  const { user } = useAuth();
  const [communities, setCommunities] = useState([]);
  const [memberships, setMemberships] = useState({});
  const [filter, setFilter] = useState('');
  const [createOpen, setCreateOpen] = useState(false);
  const [form, setForm] = useState({ name: '', description: '', category: COMMUNITY_CATEGORIES[0] });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [busyCommunity, setBusyCommunity] = useState(null);
  const [error, setError] = useState('');
  const [formError, setFormError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    communitiesApi.list().then(async (data) => {
      const membershipResults = await Promise.all(data.map(async (community) => {
        const members = await communitiesApi.members(community.id);
        return [community.id, members.some((member) => String(member.userId) === String(user?.id))];
      }));
      if (active) {
        setCommunities(data);
        setMemberships(Object.fromEntries(membershipResults));
      }
    }).catch((loadError) => {
      if (active) setError(loadError.message);
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [user?.id]);

  const filtered = useMemo(() => communities.filter((community) =>
    !filter || community.category === filter
  ), [communities, filter]);

  const createCommunity = async (event) => {
    event.preventDefault();
    setFormError('');
    if (!form.name.trim()) {
      setFormError('A community name is required.');
      return;
    }
    setSubmitting(true);
    try {
      const created = await communitiesApi.create({
        name: form.name.trim(),
        description: form.description.trim(),
        category: form.category,
      });
      setForm({ name: '', description: '', category: COMMUNITY_CATEGORIES[0] });
      setCreateOpen(false);
      setCommunities((current) => [created, ...current]);
      setMemberships((current) => ({ ...current, [created.id]: true }));
    } catch (createError) {
      setFormError(createError.message);
    } finally {
      setSubmitting(false);
    }
  };

  const toggleMembership = async (community) => {
    setBusyCommunity(community.id);
    setError('');
    try {
      if (memberships[community.id]) {
        await communitiesApi.leave(community.id);
        setMemberships((current) => ({ ...current, [community.id]: false }));
        setCommunities((current) => current.map((item) => item.id === community.id
          ? { ...item, memberCount: Math.max(0, item.memberCount - 1) } : item));
      } else {
        const updated = await communitiesApi.join(community.id);
        setMemberships((current) => ({ ...current, [community.id]: true }));
        setCommunities((current) => current.map((item) => item.id === updated.id ? updated : item));
      }
    } catch (membershipError) {
      setError(membershipError.message);
    } finally {
      setBusyCommunity(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-semibold text-brand-700">Find your people</p>
          <h1 className="mt-1 font-display text-3xl font-extrabold tracking-tight">Communities</h1>
        </div>
        <Button icon={Plus} onClick={() => setCreateOpen((open) => !open)}>Create community</Button>
      </div>

      {createOpen && <Card>
        <h2 className="mb-4 font-display text-xl font-bold">Start a community</h2>
        <form className="space-y-4" onSubmit={createCommunity}>
          <Input id="community-name" label="Name" value={form.name} maxLength={100}
            onChange={(event) => setForm({ ...form, name: event.target.value })} required />
          <div>
            <label htmlFor="community-description" className="mb-1.5 block text-sm font-semibold">Description</label>
            <textarea id="community-description" value={form.description} maxLength={2000}
              onChange={(event) => setForm({ ...form, description: event.target.value })}
              rows={3} className="w-full rounded-lg border border-brand-900/20 bg-white px-3 py-2 text-sm" />
          </div>
          <div>
            <label htmlFor="community-category" className="mb-1.5 block text-sm font-semibold">Category</label>
            <select id="community-category" value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })}
              className="h-11 w-full rounded-[10px] border border-brand-900/20 bg-white px-3 text-sm sm:max-w-sm">
              {COMMUNITY_CATEGORIES.map((category) => <option key={category} value={category}>{labelCategory(category)}</option>)}
            </select>
          </div>
          {formError && <Alert type="error">{formError}</Alert>}
          <Button type="submit" loading={submitting}>Create community</Button>
        </form>
      </Card>}

      <div className="flex flex-wrap gap-2" aria-label="Filter communities by category">
        <button type="button" aria-pressed={!filter} onClick={() => setFilter('')}
          className={`rounded-full px-4 py-2 text-sm font-semibold ${!filter ? 'bg-brand-800 text-white' : 'bg-white text-ink/65'}`}>All</button>
        {COMMUNITY_CATEGORIES.map((category) => (
          <button key={category} type="button" aria-pressed={filter === category} onClick={() => setFilter(category)}
            className={`rounded-full px-4 py-2 text-sm font-semibold ${filter === category ? 'bg-brand-800 text-white' : 'bg-white text-ink/65'}`}>
            {labelCategory(category)}
          </button>
        ))}
      </div>

      {error && <Alert type="error">{error}</Alert>}
      {loading ? <div className="flex justify-center py-12"><Spinner size="lg" className="text-brand-700" /></div>
        : filtered.length ? <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {filtered.map((community) => {
            const isOwner = String(community.ownerId) === String(user?.id);
            return <Card key={community.id} className="flex h-full flex-col">
              <span className="w-fit rounded-full bg-brand-50 px-3 py-1 text-xs font-semibold text-brand-800">{labelCategory(community.category)}</span>
              <h2 className="mt-4 font-display text-lg font-bold">{community.name}</h2>
              <p className="mt-2 flex-1 text-sm leading-relaxed text-ink/65">{community.description || 'A place to discuss shared interests and decisions.'}</p>
              <p className="mt-4 text-xs text-ink/55">{community.memberCount} members · Hosted by {community.ownerName || 'a member'}</p>
              <Button className="mt-4" variant={memberships[community.id] ? 'outline' : 'primary'}
                loading={busyCommunity === community.id} disabled={isOwner}
                onClick={() => toggleMembership(community)}>
                {isOwner ? 'Community owner' : memberships[community.id] ? 'Leave community' : 'Join community'}
              </Button>
            </Card>;
          })}
        </div> : <p className="rounded-xl border border-dashed border-brand-900/20 p-8 text-center text-sm text-ink/60">No communities in this category yet.</p>}
    </div>
  );
};

export default CommunitiesPage;
