/**
 * 咖啡向导 Context
 */

import React, { createContext, useContext, useState } from 'react';
import type { ReactNode } from 'react';

interface CoffeeGuideContextType {
  isExpanded: boolean;
  toggle: () => void;
  setExpanded: (expanded: boolean) => void;
}

const CoffeeGuideContext = createContext<CoffeeGuideContextType | undefined>(undefined);

export const useCoffeeGuide = () => {
  const context = useContext(CoffeeGuideContext);
  if (!context) {
    throw new Error('useCoffeeGuide must be used within CoffeeGuideProvider');
  }
  return context;
};

interface CoffeeGuideProviderProps {
  children: ReactNode;
}

export const CoffeeGuideProvider: React.FC<CoffeeGuideProviderProps> = ({ children }) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const toggle = () => setIsExpanded((prev) => !prev);
  const setExpanded = (expanded: boolean) => setIsExpanded(expanded);

  return (
    <CoffeeGuideContext.Provider value={{ isExpanded, toggle, setExpanded }}>
      {children}
    </CoffeeGuideContext.Provider>
  );
};
