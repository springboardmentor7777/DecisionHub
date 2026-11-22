
import { useState } from "react";
import {
  ArrowLeftRight,
  Plus,
  Scale,
  Clock3,
  Wallet,
  TrendingUp,
  ShieldAlert,
  Sparkles,
  CheckCircle2,
  Target,
  GitCompareArrows,
  Lightbulb,
  ArrowUpRight,
  ArrowLeft,
} from "lucide-react";

const factors = [
  {
    name: "Cost",
    description: "Estimated financial expense",
    icon: Wallet,
    weight: "20%",
    color: "violet",
  },
  {
    name: "Benefits",
    description: "Expected value and advantages",
    icon: TrendingUp,
    weight: "25%",
    color: "emerald",
  },
  {
    name: "Risk",
    description: "Potential challenges and uncertainty",
    icon: ShieldAlert,
    weight: "20%",
    color: "rose",
  },
  {
    name: "Time",
    description: "Time required to complete",
    icon: Clock3,
    weight: "15%",
    color: "blue",
  },
  {
    name: "Convenience",
    description: "Ease of use and accessibility",
    icon: Sparkles,
    weight: "20%",
    color: "amber",
  },
];

const iconStyles = {
  violet: "bg-violet-50 text-violet-600",
  emerald: "bg-emerald-50 text-emerald-600",
  rose: "bg-rose-50 text-rose-600",
  blue: "bg-blue-50 text-blue-600",
  amber: "bg-amber-50 text-amber-600",
};

function OptionComparison() {
  const [comparisonTitle, setComparisonTitle] = useState("");
  const [currentStep, setCurrentStep] = useState(0);

  const [options, setOptions] = useState([
    { id: 1, name: "" },
    { id: 2, name: "" },
  ]);

  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("");

  // Add another option
  const addOption = () => {
    setOptions((prev) => [
      ...prev,
      {
        id: Date.now(),
        name: "",
      },
    ]);

    setMessage("");
  };

  // Update option name
  const updateOption = (id, value) => {
    setOptions((prev) =>
      prev.map((option) =>
        option.id === id
          ? { ...option, name: value }
          : option
      )
    );

    setMessage("");
  };

  // Continue to comparison workspace
  const handleContinue = () => {
    const validOptions = options.filter(
      (option) => option.name.trim() !== ""
    );

    if (!comparisonTitle.trim()) {
      setMessage("Please enter a comparison title.");
      setMessageType("error");
      return;
    }

    if (validOptions.length < 2) {
      setMessage("Please enter at least two options.");
      setMessageType("error");
      return;
    }

    if (validOptions.length !== options.length) {
      setMessage("Please fill in all option names before continuing.");
      setMessageType("error");
      return;
    }

    setMessage("");
    setCurrentStep(1);

    console.log("Comparison Details:", {
      title: comparisonTitle,
      options: validOptions,
      factors,
    });
  };

  // --------------------------------
  // STEP 2: COMPARISON WORKSPACE
  // --------------------------------

  if (currentStep === 1) {
    return (
      <section className="min-h-full space-y-7 pb-8">

        {/* Workspace Hero */}
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-indigo-700 via-violet-700 to-purple-600 p-7 text-white shadow-xl sm:p-9">
          <div className="absolute -right-10 -top-16 h-64 w-64 rounded-full bg-white/10 blur-3xl" />

          <div className="relative z-10">
            <div className="mb-4 flex items-center gap-2 text-sm font-medium text-violet-100">
              <GitCompareArrows size={16} />
              WORKSPACE / COMPARISON
            </div>

            <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
              Comparison Workspace
            </h1>

            <p className="mt-3 text-sm text-violet-100 sm:text-base">
              {comparisonTitle}
            </p>
          </div>
        </div>

        {/* Selected Alternatives */}
        <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-5 flex items-center justify-between gap-3">
            <div>
              <h2 className="text-lg font-bold text-slate-900">
                Your Alternatives
              </h2>

              <p className="mt-1 text-sm text-slate-500">
                Options selected for this comparison.
              </p>
            </div>

            <span className="rounded-full bg-violet-50 px-3 py-1.5 text-xs font-semibold text-violet-700">
              {options.length} Options
            </span>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            {options.map((option, index) => (
              <div
                key={option.id}
                className="rounded-2xl border border-violet-100 bg-violet-50/50 p-5"
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold tracking-wider text-violet-600">
                    OPTION {String(index + 1).padStart(2, "0")}
                  </span>

                  <Target size={20} className="text-violet-600" />
                </div>

                <h3 className="mt-3 text-lg font-bold text-slate-800">
                  {option.name}
                </h3>
              </div>
            ))}
          </div>
        </div>

        {/* Evaluation Factors */}
        <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
              <Lightbulb size={23} />
            </div>

            <div>
              <h2 className="text-lg font-bold text-slate-900">
                Evaluation Factors
              </h2>

              <p className="mt-1 text-sm text-slate-500">
                Factors used to evaluate your alternatives.
              </p>
            </div>
          </div>

          <div className="mt-6 grid gap-3 sm:grid-cols-2">
            {factors.map(
              ({ name, description, icon: Icon, weight, color }) => (
                <div
                  key={name}
                  className="flex items-center gap-3 rounded-2xl border border-slate-100 bg-slate-50/70 p-4"
                >
                  <div
                    className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${iconStyles[color]}`}
                  >
                    <Icon size={20} />
                  </div>

                  <div className="min-w-0 flex-1">
                    <h3 className="text-sm font-bold text-slate-800">
                      {name}
                    </h3>

                    <p className="mt-1 text-xs text-slate-500">
                      {description}
                    </p>
                  </div>

                  <span className="rounded-lg bg-white px-2.5 py-1.5 text-xs font-bold text-slate-600 shadow-sm">
                    {weight}
                  </span>
                </div>
              )
            )}
          </div>
        </div>

        {/* Pros and Cons Preview */}
        <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600">
              <CheckCircle2 size={23} />
            </div>

            <div>
              <h2 className="text-lg font-bold text-slate-900">
                Next: Pros & Cons
              </h2>

              <p className="mt-1 text-sm text-slate-500">
                Evaluate the advantages and disadvantages of each option.
              </p>
            </div>
          </div>

          <div className="mt-5 rounded-2xl bg-violet-50 p-4">
            <p className="text-sm leading-6 text-violet-800">
              Your comparison setup is ready. The next stage will let you
              record the pros and cons of every alternative.
            </p>
          </div>
        </div>

        {/* Back Button */}
        <button
          type="button"
          onClick={() => setCurrentStep(0)}
          className="inline-flex items-center gap-2 rounded-xl border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
        >
          <ArrowLeft size={17} />
          Back to setup
        </button>

      </section>
    );
  }

  // --------------------------------
  // STEP 1: COMPARISON SETUP
  // --------------------------------

  return (
    <section className="min-h-full space-y-7 pb-8">

      {/* Hero */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-indigo-700 via-violet-700 to-purple-600 p-7 text-white shadow-xl shadow-violet-200/50 sm:p-9">
        <div className="absolute -right-10 -top-16 h-64 w-64 rounded-full bg-white/10 blur-3xl" />
        <div className="absolute bottom-[-90px] right-1/3 h-52 w-52 rounded-full bg-indigo-300/20 blur-3xl" />

        <div className="relative z-10 flex flex-wrap items-center justify-between gap-6">
          <div>
            <div className="mb-4 flex items-center gap-2 text-sm font-medium text-violet-100">
              <GitCompareArrows size={16} />
              WORKSPACE / COMPARISON
            </div>

            <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
              Compare Options
            </h1>

            <p className="mt-3 max-w-lg text-sm leading-6 text-violet-100 sm:text-base">
              Think clearly, compare smarter and discover the option that
              truly works best for you.
            </p>
          </div>

          <div className="flex h-20 w-20 items-center justify-center rounded-3xl border border-white/20 bg-white/10 backdrop-blur-sm">
            <ArrowLeftRight size={38} strokeWidth={1.6} />
          </div>
        </div>
      </div>

      {/* Comparison Setup */}
      <div className="overflow-hidden rounded-3xl border border-slate-200/80 bg-white shadow-sm">
        <div className="flex items-center gap-4 border-b border-slate-100 p-6">
          <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-violet-100 text-violet-700">
            <Scale size={23} />
          </div>

          <div>
            <h2 className="text-lg font-bold text-slate-900">
              New comparison
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Set up your alternatives to get started.
            </p>
          </div>
        </div>

        <div className="space-y-6 p-6">

          {/* Comparison Title */}
          <div>
            <label className="mb-2 block text-sm font-semibold text-slate-700">
              Comparison title
            </label>

            <input
              type="text"
              value={comparisonTitle}
              onChange={(e) => {
                setComparisonTitle(e.target.value);
                setMessage("");
              }}
              placeholder="e.g. Choosing my next laptop"
              className="w-full rounded-xl border border-slate-200 bg-slate-50/70 px-4 py-3.5 text-sm outline-none transition focus:border-violet-400 focus:bg-white focus:ring-4 focus:ring-violet-100"
            />
          </div>

          {/* Alternatives */}
          <div>
            <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
              <div>
                <h3 className="text-sm font-bold text-slate-800">
                  Your alternatives
                </h3>

                <p className="mt-1 text-xs text-slate-500">
                  Add the options you want to evaluate.
                </p>
              </div>

              <span className="rounded-full bg-violet-50 px-3 py-1 text-xs font-semibold text-violet-700">
                {options.length}{" "}
                {options.length === 1 ? "option" : "options"}
              </span>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              {options.map((option, index) => (
                <div
                  key={option.id}
                  className="group rounded-2xl border border-slate-200 bg-slate-50/60 p-5 transition hover:border-violet-200 hover:bg-violet-50/30"
                >
                  <div className="mb-4 flex items-center justify-between">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-400">
                      OPTION {String(index + 1).padStart(2, "0")}
                    </span>

                    <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-white text-violet-600 shadow-sm">
                      <Target size={18} />
                    </div>
                  </div>

                  <input
                    type="text"
                    value={option.name}
                    onChange={(e) =>
                      updateOption(option.id, e.target.value)
                    }
                    placeholder={`Enter option ${index + 1}`}
                    className="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none transition focus:border-violet-400 focus:ring-4 focus:ring-violet-100"
                  />
                </div>
              ))}
            </div>

            {/* Add Option */}
            <button
              type="button"
              onClick={addOption}
              className="mt-4 inline-flex items-center gap-2 rounded-xl border border-dashed border-violet-300 px-4 py-3 text-sm font-semibold text-violet-700 transition hover:border-violet-500 hover:bg-violet-50"
            >
              <Plus size={17} />
              Add another option
            </button>
          </div>
        </div>
      </div>

      {/* Evaluation Factors */}
      <div className="rounded-3xl border border-slate-200/80 bg-white p-6 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600">
              <Lightbulb size={23} />
            </div>

            <div>
              <h2 className="text-lg font-bold text-slate-900">
                Evaluation factors
              </h2>

              <p className="mt-1 text-sm text-slate-500">
                What matters most in your decision?
              </p>
            </div>
          </div>

          <span className="rounded-full bg-emerald-50 px-3 py-1.5 text-xs font-semibold text-emerald-700">
            <CheckCircle2 size={13} className="mr-1 inline" />
            5 factors
          </span>
        </div>

        <div className="mt-6 grid gap-3 sm:grid-cols-2">
          {factors.map(
            ({ name, description, icon: Icon, weight, color }) => (
              <div
                key={name}
                className="flex items-center gap-3 rounded-2xl border border-slate-100 bg-slate-50/70 p-4 transition hover:border-violet-200 hover:bg-white hover:shadow-sm"
              >
                <div
                  className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${iconStyles[color]}`}
                >
                  <Icon size={20} />
                </div>

                <div className="min-w-0 flex-1">
                  <h3 className="text-sm font-bold text-slate-800">
                    {name}
                  </h3>

                  <p className="mt-1 text-xs text-slate-500">
                    {description}
                  </p>
                </div>

                <span className="rounded-lg bg-white px-2.5 py-1.5 text-xs font-bold text-slate-600 shadow-sm">
                  {weight}
                </span>
              </div>
            )
          )}
        </div>

        <div className="mt-5 flex items-start gap-3 rounded-2xl bg-violet-50 p-4">
          <Sparkles size={19} className="mt-0.5 shrink-0 text-violet-600" />

          <p className="text-xs leading-6 text-violet-800">
            These are suggested evaluation weights for the design preview.
            Later, you can customize the importance of each factor to match
            your priorities.
          </p>
        </div>
      </div>

      {/* Bottom Action */}
      <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div>
          <h3 className="font-bold text-slate-900">
            Ready to evaluate?
          </h3>

          <p className="mt-1 text-sm text-slate-500">
            Your comparison setup is ready for the next step.
          </p>
        </div>

        <button
          type="button"
          onClick={handleContinue}
          className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-violet-200 transition hover:-translate-y-0.5 hover:bg-violet-700"
        >
          Continue comparison
          <ArrowUpRight size={17} />
        </button>
      </div>

      {/* Validation Message */}
      {message && (
        <div
          role="status"
          className={`rounded-xl border p-4 text-sm font-medium ${
            messageType === "error"
              ? "border-red-200 bg-red-50 text-red-700"
              : "border-emerald-200 bg-emerald-50 text-emerald-700"
          }`}
        >
          {message}
        </div>
      )}

    </section>
  );
}

export default OptionComparison;
