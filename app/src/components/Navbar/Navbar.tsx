import React from 'react';
import './Navbar.css';

interface NavbarProps {
  navOptions: NavPairArray;
};

interface NavPair {
  name: string;
  url: string;
};

export type NavPairArray = Array<NavPair>

function Navbar(props: NavbarProps) {

  return (
    <div id="navbar">
      {props.navOptions.map(navItem => {
        return (
          <div className="nav-item">
            <a href={navItem.url}>{navItem.name}</a>
          </div>
        );
      })}
    </div>
  );
}

export default Navbar;