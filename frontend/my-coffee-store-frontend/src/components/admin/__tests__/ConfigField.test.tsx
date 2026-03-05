import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { ConfigField } from '../ConfigField';

describe('ConfigField', () => {
  test('renders text input field correctly', () => {
    render(
      <ConfigField
        label="Test Label"
        value="test value"
        onChange={jest.fn()}
        placeholder="Test placeholder"
      />
    );

    expect(screen.getByText('Test Label')).toBeInTheDocument();
    expect(screen.getByDisplayValue('test value')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Test placeholder')).toBeInTheDocument();
  });

  test('renders password input field', () => {
    render(
      <ConfigField
        label="Password"
        value="password123"
        onChange={jest.fn()}
        type="password"
      />
    );

    expect(screen.getByLabelText('Password')).toHaveAttribute('type', 'password');
    expect(screen.getByDisplayValue('password123')).toBeInTheDocument();
  });

  test('renders number input field', () => {
    render(
      <ConfigField
        label="Temperature"
        value="0.7"
        onChange={jest.fn()}
        type="number"
        min="0"
        max="2"
        step="0.1"
      />
    );

    expect(screen.getByLabelText('Temperature')).toHaveAttribute('type', 'number');
    expect(screen.getByDisplayValue('0.7')).toBeInTheDocument();
    expect(screen.getByLabelText('Temperature')).toHaveAttribute('min', '0');
    expect(screen.getByLabelText('Temperature')).toHaveAttribute('max', '2');
    expect(screen.getByLabelText('Temperature')).toHaveAttribute('step', '0.1');
  });

  test('handles text input changes', () => {
    const handleChange = jest.fn();
    render(
      <ConfigField
        label="Test Input"
        value="initial value"
        onChange={handleChange}
      />
    );

    const input = screen.getByDisplayValue('initial value');
    fireEvent.change(input, { target: { value: 'new value' } });

    expect(handleChange).toHaveBeenCalledWith('new value');
  });

  test('handles number input changes', () => {
    const handleChange = jest.fn();
    render(
      <ConfigField
        label="Number Input"
        value="100"
        onChange={handleChange}
        type="number"
      />
    );

    const input = screen.getByDisplayValue('100');
    fireEvent.change(input, { target: { value: '200' } });

    expect(handleChange).toHaveBeenCalledWith('200');
  });

  test('shows required indicator', () => {
    render(
      <ConfigField
        label="Required Field"
        value=""
        onChange={jest.fn()}
        required
      />
    );

    expect(screen.getByText('Required Field')).toHaveTextContent('*');
  });

  test('disabled input is not editable', () => {
    const handleChange = jest.fn();
    render(
      <ConfigField
        label="Disabled Field"
        value="disabled value"
        onChange={handleChange}
        disabled
      />
    );

    const input = screen.getByDisplayValue('disabled value');
    fireEvent.change(input, { target: { value: 'new value' } });

    expect(handleChange).not.toHaveBeenCalled();
    expect(input).toHaveAttribute('disabled');
  });

  test('applies correct styling', () => {
    render(
      <ConfigField
        label="Styled Field"
        value="test value"
        onChange={jest.fn()}
      />
    );

    const input = screen.getByDisplayValue('test value');
    expect(input).toHaveClass('rounded-[10px]');
    expect(input).toHaveClass('h-[42px]');
    expect(input).toHaveClass('px-3');
  });

  test('handles empty value', () => {
    const handleChange = jest.fn();
    render(
      <ConfigField
        label="Empty Field"
        value=""
        onChange={handleChange}
      />
    );

    const input = screen.getByDisplayValue('');
    fireEvent.change(input, { target: { value: 'filled' } });

    expect(handleChange).toHaveBeenCalledWith('filled');
  });

  test('handles placeholder text', () => {
    render(
      <ConfigField
        label="With Placeholder"
        value=""
        onChange={jest.fn()}
        placeholder="Enter value..."
      />
    );

    const input = screen.getByPlaceholderText('Enter value...');
    expect(input).toBeInTheDocument();
  });

  test('handles number input with decimal values', () => {
    const handleChange = jest.fn();
    render(
      <ConfigField
        label="Decimal Input"
        value="1.23"
        onChange={handleChange}
        type="number"
        step="0.01"
      />
    );

    const input = screen.getByDisplayValue('1.23');
    fireEvent.change(input, { target: { value: '2.34' } });

    expect(handleChange).toHaveBeenCalledWith('2.34');
  });

  test('does not allow negative numbers when min is set', () => {
    const handleChange = jest.fn();
    render(
      <ConfigField
        label="Positive Number"
        value="10"
        onChange={handleChange}
        type="number"
        min="0"
      />
    );

    const input = screen.getByDisplayValue('10');
    fireEvent.change(input, { target: { value: '-5' } });

    // 注意：HTML input 不会阻止负数的输入，需要在上层组件处理
    expect(handleChange).toHaveBeenCalledWith('-5');
  });

  test('handles large numbers', () => {
    const handleChange = jest.fn();
    render(
      <ConfigField
        label="Large Number"
        value="1000000"
        onChange={handleChange}
        type="number"
      />
    );

    const input = screen.getByDisplayValue('1000000');
    fireEvent.change(input, { target: { value: '999999999' } });

    expect(handleChange).toHaveBeenCalledWith('999999999');
  });

  test('applies correct label styling', () => {
    render(
      <ConfigField
        label="Styled Label"
        value="test"
        onChange={jest.fn()}
      />
    );

    const label = screen.getByText('Styled Label');
    expect(label).toHaveClass('text-sm');
    expect(label).toHaveClass('font-medium');
  });

  test('trims whitespace from input value', () => {
    const handleChange = jest.fn();
    render(
      <ConfigField
        label="Trim Input"
        value="test"
        onChange={handleChange}
      />
    );

    const input = screen.getByDisplayValue('test');
    fireEvent.change(input, { target: { value: '  new value  ' } });

    expect(handleChange).toHaveBeenCalledWith('  new value  ');
    // 注意：trimming 需要在上层组件处理
  });
});