import { Users } from 'lucide-react';
const TeamsPage = () => (
  <div className="flex flex-col items-center justify-center py-24 text-center">
    <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-emerald-500/10">
      <Users className="h-8 w-8 text-emerald-500" />
    </div>
    <h2 className="text-xl font-semibold text-foreground">Teams</h2>
    <p className="mt-2 max-w-sm text-sm text-muted-foreground">Team management coming in the next sprint.</p>
  </div>
);
export default TeamsPage;
