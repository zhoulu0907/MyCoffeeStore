/**
 * 应用主组件
 */

import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, CartProvider } from './contexts';
import { CoffeeGuideProvider } from './contexts/CoffeeGuideContext';
import { Home, Login, Register, CoffeeList, CoffeeDetail, Cart, Checkout, Order, Profile } from './pages';
import { CoffeeGuide } from './components';
import { ROUTES } from './utils/constants';

function App() {
  return (
    <AuthProvider>
      <CoffeeGuideProvider>
        <CartProvider>
          <Routes>
            {/* 首页 */}
            <Route path={ROUTES.HOME} element={<Home />} />

            {/* 认证页面 */}
            <Route path={ROUTES.LOGIN} element={<Login />} />
            <Route path={ROUTES.REGISTER} element={<Register />} />

            {/* 咖啡相关页面 */}
            <Route path={ROUTES.COFFEE_LIST} element={<CoffeeList />} />
            <Route path={ROUTES.COFFEE_DETAIL} element={<CoffeeDetail />} />

            {/* 购物车和订单 */}
            <Route path={ROUTES.CART} element={<Cart />} />
            <Route path={ROUTES.CHECKOUT} element={<Checkout />} />
            <Route path={ROUTES.ORDER} element={<Order />} />
            <Route path={ROUTES.PROFILE} element={<Profile />} />

            {/* 默认重定向到首页 */}
            <Route path="*" element={<Navigate to={ROUTES.HOME} replace />} />
          </Routes>

          {/* 咖啡向导 */}
          <CoffeeGuide />
        </CartProvider>
      </CoffeeGuideProvider>
    </AuthProvider>
  );
}

export default App;
