/**
 * 配置字段组件 - 用于 LLM 配置表单
 */

import React from 'react';

interface ConfigFieldProps {
  label: string;
  value: string | number;
  onChange: (value: string) => void;
  type?: 'text' | 'password' | 'number';
  placeholder?: string;
  disabled?: boolean;
  required?: boolean;
  step?: string;
  min?: string;
  max?: string;
}

const ConfigField: React.FC<ConfigFieldProps> = ({
  label,
  value,
  onChange,
  type = 'text',
  placeholder,
  disabled = false,
  required = false,
  step,
  min,
  max,
}) => {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(e.target.value);
  };

  return (
    <div className="flex flex-col gap-2">
      <label className="text-sm font-bold" style={{ color: '#5E4338' }}>
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <input
        type={type}
        value={value}
        onChange={handleChange}
        placeholder={placeholder}
        disabled={disabled}
        required={required}
        step={step}
        min={min}
        max={max}
        className="w-full h-[42px] px-3 rounded-[10px] text-[13px] font-normal outline-none focus:ring-2 focus:ring-primary/20 disabled:opacity-60 disabled:cursor-not-allowed"
        style={{
          backgroundColor: '#F2ECE5',
          color: '#2A1A15',
          border: '1px solid transparent',
        }}
      />
    </div>
  );
};

export default ConfigField;
