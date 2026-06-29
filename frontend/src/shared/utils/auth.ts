export const getStoredToken = (): string | null => {
  return localStorage.getItem('forgemind_token');
};

export const setStoredToken = (token: string): void => {
  localStorage.setItem('forgemind_token', token);
};

export const removeStoredToken = (): void => {
  localStorage.removeItem('forgemind_token');
};
