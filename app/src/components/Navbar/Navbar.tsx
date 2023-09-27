import React from 'react';
import './Navbar.css';
import type { MenuProps } from 'antd';
import { DatabaseOutlined, HomeOutlined, LineChartOutlined, SettingOutlined } from '@ant-design/icons';
import { Menu } from 'antd';
import { changePage } from '../../features/pageSlice';
import { useDispatch } from 'react-redux';

type MenuItem = Required<MenuProps>['items'][number];

function getItem(
  label: React.ReactNode,
  key: React.Key,
  icon?: React.ReactNode,
  children?: MenuItem[],
  type?: 'group',
): MenuItem {
  return {
    key,
    icon,
    children,
    label,
    type,
  } as MenuItem;
}

const items: MenuProps['items'] = [
  getItem('Home', 'home', <HomeOutlined />),
  getItem('Statistics', 'statistics', <LineChartOutlined />),
  getItem('Database', 'database', <DatabaseOutlined />),
  getItem('Settings', 'settings', <SettingOutlined />),
];

const Navbar: React.FC = () => {
  // For page changing.
  const dispatch = useDispatch();

  const onClick: MenuProps['onClick'] = (e) => {
    console.log("Clicked: " + e.key)
    if (!e.key) return;
    if (e.key === 'home') dispatch(changePage(''));
    else dispatch(changePage(e.key));
  };

  return (
    <Menu
      onClick={onClick}
      style={{ width: 350 }}
      defaultSelectedKeys={['1']}
      defaultOpenKeys={['sub1']}
      mode="inline"
      items={items}
    />
  );
};

export default Navbar;