import { useAuth } from '@/features/auth/hooks/useAuth';
const ProfilePage = () => {
  const { user } = useAuth();
  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-2xl font-bold text-foreground">Profile</h1>
      {user && (
        <div className="rounded-xl border border-border bg-card p-6 space-y-3">
          <p className="text-muted-foreground text-sm">Name: <span className="text-foreground font-medium">{user.firstName} {user.lastName}</span></p>
          <p className="text-muted-foreground text-sm">Username: <span className="text-foreground font-medium">{user.username}</span></p>
          <p className="text-muted-foreground text-sm">Email: <span className="text-foreground font-medium">{user.email}</span></p>
          <p className="text-muted-foreground text-sm">Roles: <span className="text-foreground font-medium">{user.role.join(', ')}</span></p>
        </div>
      )}
    </div>
  );
};
export default ProfilePage;
