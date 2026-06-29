import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import userEvent from '@testing-library/user-event';
import { Input } from './Input';

describe('Input component', () => {
  it('renders correctly', () => {
    render(<Input placeholder="Enter username" />);
    expect(screen.getByPlaceholderText(/enter username/i)).toBeInTheDocument();
  });

  it('handles user input', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    
    render(<Input placeholder="Enter text" onChange={handleChange} />);
    const input = screen.getByPlaceholderText(/enter text/i);
    
    await user.type(input, 'hello');
    
    expect(input).toHaveValue('hello');
    expect(handleChange).toHaveBeenCalled();
  });

  it('is disabled when disabled prop is passed', () => {
    render(<Input placeholder="Disabled input" disabled />);
    
    const input = screen.getByPlaceholderText(/disabled input/i);
    expect(input).toBeDisabled();
  });
});
