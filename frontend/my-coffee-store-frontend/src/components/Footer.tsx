/**
 * 页脚组件 - Haight Ashbury Cafe
 */

import React from 'react';
import { Link } from 'react-router-dom';

const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer style={{ backgroundColor: '#1A110E' }} className="py-12 px-6">
      <div className="max-w-7xl mx-auto">
        <div className="flex flex-col md:flex-row justify-between items-center gap-6">
          {/* 品牌信息 */}
          <div className="text-center md:text-left">
            <h3 className="font-georgia text-2xl font-bold mb-2" style={{ color: '#F2E8DC' }}>
              Haight Ashbury Cafe
            </h3>
            <p className="text-gray-400 text-sm max-w-md">
              位于旧金山 Haight Ashbury 的精品咖啡店，我们致力于为每一位顾客提供最优质的咖啡体验。
            </p>
          </div>

          {/* 快速链接 */}
          <div>
            <h4 className="font-semibold mb-3 text-white">快速链接</h4>
            <ul className="space-y-2 text-sm">
              <li>
                <Link
                  to="/"
                  className="hover:text-accent transition-colors"
                  style={{ color: '#CDBAA4' }}
                >
                  首页
                </Link>
              </li>
              <li>
                <Link
                  to="/coffee"
                  className="hover:text-accent transition-colors"
                  style={{ color: '#CDBAA4' }}
                >
                  菜单
                </Link>
              </li>
              <li>
                <Link
                  to="/cart"
                  className="hover:text-accent transition-colors"
                  style={{ color: '#CDBAA4' }}
                >
                  购物车
                </Link>
              </li>
            </ul>
          </div>

          {/* 联系信息 */}
          <div>
            <h4 className="font-semibold mb-3 text-white">联系我们</h4>
            <ul className="space-y-2 text-sm" style={{ color: '#CDBAA4' }}>
              <li className="flex items-center">
                <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                </svg>
                <span>1452 Haight St<br />San Francisco, CA 94117</span>
              </li>
              <li className="flex items-center">
                <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z" />
                </svg>
                <span>+1 (415) 555-0136</span>
              </li>
            </ul>
          </div>
        </div>

        {/* 底部版权信息 */}
        <div className="mt-8 pt-6 border-t border-gray-800 text-center text-sm" style={{ color: '#CDBAA4' }}>
          <p>© {currentYear} Haight Ashbury Cafe. Brew Bold, Stay Local.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
