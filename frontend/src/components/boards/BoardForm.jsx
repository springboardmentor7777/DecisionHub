import React, { useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { CATEGORIES, POLL_TYPES, pollLabel } from '../../api/boards';
import Button from '../common/Button';
import Input from '../common/Input';
import Alert from '../common/Alert';

const emptyOption = () => ({ name: '', pros: '', cons: '', score: 50 });

const BoardForm = ({
  initialValues,
  mode = 'create',
  onSubmit,
  submitLabel = mode === 'edit' ? 'Save changes' : 'Create board',
}) => {
  const [values, setValues] = useState(() => ({
    title: initialValues?.title || '',
    description: initialValues?.description || '',
    category: initialValues?.category || CATEGORIES[0],
    visibility: initialValues?.visibility || 'PUBLIC',
    pollType: initialValues?.pollType || POLL_TYPES[0],
    options: initialValues?.options?.map((option) => ({
      name: option.name || '',
      pros: option.pros || '',
      cons: option.cons || '',
      score: option.score ?? 50,
    })) || [emptyOption(), emptyOption()],
  }));
  const [validationError, setValidationError] = useState('');
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const update = (field, value) => setValues((current) => ({ ...current, [field]: value }));
  const updateOption = (index, field, value) => {
    setValues((current) => ({
      ...current,
      options: current.options.map((option, i) => i === index ? { ...option, [field]: value } : option),
    }));
  };

  const submit = async (event) => {
    event.preventDefault();
    setValidationError('');
    setSubmitError('');
    if (!values.title.trim()) {
      setValidationError('A title is required.');
      return;
    }
    if (mode === 'create' && values.options.length < 2) {
      setValidationError('Add at least two options.');
      return;
    }
    if (mode === 'create' && values.options.length > 20) {
      setValidationError('A board can have no more than 20 options.');
      return;
    }
    if (mode === 'create') {
      if (values.options.some((option) => !option.name.trim())) {
        setValidationError('Enter a name for each option.');
        return;
      }
      if (values.options.some((option) =>
        option.score === '' || !Number.isInteger(Number(option.score))
        || Number(option.score) < 0 || Number(option.score) > 100
      )) {
        setValidationError('Each option score must be a whole number between 0 and 100.');
        return;
      }
    }

    const payload = {
      title: values.title.trim(),
      description: values.description.trim(),
      category: values.category,
      visibility: values.visibility,
      pollType: values.pollType,
      ...(mode === 'create' ? {
        options: values.options.map((option) => ({
          name: option.name.trim(),
          pros: option.pros.trim(),
          cons: option.cons.trim(),
          score: Number(option.score),
        })),
      } : {}),
    };

    setSubmitting(true);
    try {
      await onSubmit(payload);
    } catch (error) {
      setSubmitError(error.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={submit} className="space-y-5">
      {validationError && <Alert type="error">{validationError}</Alert>}
      {submitError && <Alert type="error">{submitError}</Alert>}
      <Input
        id="board-title"
        label="Decision title"
        value={values.title}
        onChange={(event) => update('title', event.target.value)}
        maxLength={160}
        required
      />
      <div>
        <label htmlFor="board-description" className="mb-1.5 block text-sm font-semibold text-ink/85">Description</label>
        <textarea
          id="board-description"
          value={values.description}
          onChange={(event) => update('description', event.target.value)}
          maxLength={3000}
          rows={3}
          className="w-full rounded-[10px] border border-brand-900/20 bg-white px-3.5 py-3 text-sm focus:border-brand-600 focus:outline-none focus:ring-4 focus:ring-brand-100"
        />
      </div>
      <div className="grid gap-4 sm:grid-cols-3">
        <div>
          <label htmlFor="board-category" className="mb-1.5 block text-sm font-semibold text-ink/85">Category</label>
          <select id="board-category" value={values.category} onChange={(event) => update('category', event.target.value)}
            className="h-11 w-full rounded-[10px] border border-brand-900/20 bg-white px-3 text-sm">
            {CATEGORIES.map((category) => <option key={category} value={category}>{category}</option>)}
          </select>
        </div>
        <div>
          <label htmlFor="board-visibility" className="mb-1.5 block text-sm font-semibold text-ink/85">Visibility</label>
          <select id="board-visibility" value={values.visibility} onChange={(event) => update('visibility', event.target.value)}
            className="h-11 w-full rounded-[10px] border border-brand-900/20 bg-white px-3 text-sm">
            <option value="PUBLIC">Public</option>
            <option value="PRIVATE">Private</option>
          </select>
        </div>
        <div>
          <label htmlFor="board-poll-type" className="mb-1.5 block text-sm font-semibold text-ink/85">Poll type</label>
          <select id="board-poll-type" value={values.pollType} onChange={(event) => update('pollType', event.target.value)}
            className="h-11 w-full rounded-[10px] border border-brand-900/20 bg-white px-3 text-sm">
            {POLL_TYPES.map((pollType) => <option key={pollType} value={pollType}>{pollLabel(pollType)}</option>)}
          </select>
        </div>
      </div>

      {mode === 'create' && (
        <fieldset className="space-y-4">
          <legend className="text-base font-bold">Options</legend>
          {values.options.map((option, index) => (
            <section key={index} className="rounded-xl border border-brand-900/10 bg-paper/70 p-4">
              <div className="mb-4 flex items-center justify-between">
                <h3 className="font-semibold">Option {index + 1}</h3>
                {values.options.length > 2 && (
                  <Button type="button" size="sm" variant="ghost" icon={Trash2}
                    onClick={() => update('options', values.options.filter((_, i) => i !== index))}>
                    Remove
                  </Button>
                )}
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <Input id={`option-${index}-name`} label="Option name" value={option.name}
                  onChange={(event) => updateOption(index, 'name', event.target.value)} maxLength={160} required />
                <Input id={`option-${index}-score`} label="Score (0-100)" type="number" min="0" max="100"
                  step="1" value={option.score} onChange={(event) => updateOption(index, 'score', event.target.value)} required />
                <div>
                  <label htmlFor={`option-${index}-pros`} className="mb-1.5 block text-sm font-semibold text-ink/85">Pros</label>
                  <textarea id={`option-${index}-pros`} value={option.pros}
                    onChange={(event) => updateOption(index, 'pros', event.target.value)} maxLength={3000} rows={2}
                    className="w-full rounded-[10px] border border-brand-900/20 bg-white px-3 py-2 text-sm" />
                </div>
                <div>
                  <label htmlFor={`option-${index}-cons`} className="mb-1.5 block text-sm font-semibold text-ink/85">Cons</label>
                  <textarea id={`option-${index}-cons`} value={option.cons}
                    onChange={(event) => updateOption(index, 'cons', event.target.value)} maxLength={3000} rows={2}
                    className="w-full rounded-[10px] border border-brand-900/20 bg-white px-3 py-2 text-sm" />
                </div>
              </div>
            </section>
          ))}
          <Button type="button" variant="outline" size="sm" icon={Plus}
            disabled={values.options.length >= 20}
            onClick={() => update('options', [...values.options, emptyOption()])}>
            {values.options.length >= 20 ? 'Maximum 20 options' : 'Add option'}
          </Button>
        </fieldset>
      )}
      {mode === 'edit' && (
        <p className="text-sm text-ink/60">Existing options are unchanged when editing board details.</p>
      )}
      <Button type="submit" loading={submitting}>{submitLabel}</Button>
    </form>
  );
};

export default BoardForm;
