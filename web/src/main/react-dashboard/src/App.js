/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, { Component } from 'react';
import './App.css';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import ProductContainer from './containers/productContainer.js';
import InfrastructureContainer from './containers/InfraContainer.js';
import ScenarioContainer from './containers/ScenarioContainer.js';
import TestCaseContainer from './containers/TestCaseContainer.js';
import DeploymentContainer from './containers/deploymentContainer.js';
import {
  Route,
  Switch,
} from 'react-router-dom';
import AppBar from 'material-ui/AppBar';
import { createStore } from 'redux';
import { Provider } from 'react-redux';
import testGrid from './reducers/testGrid.js';
import { persistStore, persistCombineReducers } from 'redux-persist';
import { PersistGate } from 'redux-persist/es/integration/react';
import storage from 'redux-persist/es/storage';
import Drawer from 'material-ui/Drawer';
import MenuItem from 'material-ui/MenuItem';
import IconButton from 'material-ui/IconButton';
import NavigationBack from 'material-ui/svg-icons/navigation/chevron-left';
import NevigationExpand from 'material-ui/svg-icons/navigation/menu';
import Paper from 'material-ui/Paper';
import FlatButton from 'material-ui/FlatButton';

const config = {
  key: 'root',
  storage,
}
const reducer = persistCombineReducers(config, testGrid);
const store = createStore(reducer);
const persistor = persistStore(store);

class App extends Component {

  handleClose = () => {
    var b = !this.state.open
    var w = b ? 240 : 20;
    this.setState({ open: b, navWidth: w });
  };

  constructor(props) {
    super(props);
    this.state = {
      open: true,
      navWidth: 240
    }
  }

  render() {
    const paperStyle = { margin: '26px 40px 50px ' + this.state.navWidth + 'px' };
    return (
      <Provider store={store}>
        <PersistGate
          persistor={persistor}>
          <MuiThemeProvider>
            <div style={{ position: 'absolute', top: '0px', right: '0px', bottom: '0px', left: '0px', backgroundColor: '#EEEEEE' }}>
              <AppBar title=" WSO2 TestGrid " style={{
                backgroundColor: '#424242',
              }}
                iconElementLeft={<IconButton onClick={this.handleClose}>{this.state.open ? <NavigationBack /> : <NevigationExpand />}</IconButton>}
                iconElementRight={<FlatButton label="Login" />} > </AppBar>
              <Drawer open={this.state.open} containerStyle={{ 'top': '64px', backgroundColor:'#BDBDBD'}} width={200} >
                <MenuItem><a href="http://ec2-34-238-28-168.compute-1.amazonaws.com:8080/blue/organizations/jenkins/pipelines/"> TestGrid AdminPortal</a></MenuItem>
              </Drawer>
              <Paper style={paperStyle} zDepth={2}>
                <Switch>
                  <Route exact path='/testgrid/v0.9/' component={ProductContainer} />
                  <Route exact path='/testgrid/v0.9/deployments/product/:productId/date/:date' component={DeploymentContainer} />
                  <Route exact path='/testgrid/v0.9/infrastructures/deployment/:deploymentid/' component={InfrastructureContainer} />
                  <Route exact path='/testgrid/v0.9/scenarios/infrastructure/:infraid' component={ScenarioContainer} />
                  <Route exact path='/testgrid/v0.9/testcases/scenario/:scenarioid' component={TestCaseContainer} />
                </Switch>
              </ Paper>
            </div>
          </MuiThemeProvider>
        </PersistGate>
      </Provider>
    );
  }

}

export default App;
