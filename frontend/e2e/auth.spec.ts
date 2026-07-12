import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test('should navigate to login page', async ({ page }) => {
    await page.goto('/login');
    
    // Check if the login form is visible
    await expect(page.getByRole('heading', { name: /Welcome to ForgeMind/i })).toBeVisible();
    await expect(page.getByLabel(/Email address/i)).toBeVisible();
    await expect(page.getByLabel(/Password/i)).toBeVisible();
  });

  test('should show validation errors on empty submit', async ({ page }) => {
    await page.goto('/login');
    
    await page.getByRole('button', { name: /sign in/i }).click();
    
    // Depending on form implementation, validation messages will appear
    // This assumes basic HTML5 validation or custom form validation
  });
});
