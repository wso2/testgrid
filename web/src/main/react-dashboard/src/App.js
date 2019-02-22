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

import React, {Component} from 'react';
import './App.css';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import ProductContainer from './containers/productContainer.js';
import InfrastructureOverviewContainer from './containers/InfraCombinationOverviewContainer.js';
import ScenarioContainer from './containers/ScenarioContainer.js';
import TestCaseContainer from './containers/TestCaseContainer.js';
import DeploymentContainer from './containers/deploymentContainer.js';
import Login from './components/Login.js'
import {Route, Switch} from 'react-router-dom';
import AppBar from 'material-ui/AppBar';
import {createStore} from 'redux';
import {Provider} from 'react-redux';
import testGrid from './reducers/testGrid.js';
import {persistCombineReducers, persistStore} from 'redux-persist';
import {PersistGate} from 'redux-persist/es/integration/react';
import storage from 'redux-persist/lib/storage/session';
import Drawer from 'material-ui/Drawer';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import IconButton from 'material-ui/IconButton';
import NavigationBack from 'material-ui/svg-icons/navigation/chevron-left';
import NevigationExpand from 'material-ui/svg-icons/navigation/menu';
import Paper from 'material-ui/Paper';
import {TESTGRID_CONTEXT} from "./constants";
import {withCookies, Cookies} from 'react-cookie';
import Toolbar from '@material-ui/core/Toolbar';
import {instanceOf} from 'prop-types';

const config = {
  key: 'root',
  storage,
};
const reducer = persistCombineReducers(config, testGrid);
const store = createStore(reducer);
const persistor = persistStore(store);

class App extends Component {
  static propTypes = {
    cookies: instanceOf(Cookies).isRequired
  };

  handleClose = () => {
    let b = !this.state.open;
    let w = b ? 240 : 20;
    this.setState({open: b, navWidth: w});
  };

  constructor(props) {
    super(props);
    const { cookies } = props;
    this.state = {
      name: cookies.get('TGUserName') || 'unknown',
      open: true,
      navWidth: 20
    }
  }

  render() {
    const paperStyle = {margin: '80px 20px 50px ' + this.state.navWidth + 'px'};
    return (
      <Provider store={store}>
        <PersistGate
          persistor={persistor}>
          <MuiThemeProvider>
            <div style={{
              position: 'absolute',
              top: '0px',
              right: '0px',
              bottom: '0px',
              left: '0px',
              backgroundColor: '#EEEEEE'
            }}>
              <AppBar
                title={
                  <a href={TESTGRID_CONTEXT + '/'} className="title">WSO2 TestGrid</a>
                }
                style={{backgroundColor: '#424242', position: 'fixed'}}
                iconElementLeft={
                  <IconButton onClick={this.handleClose}>
                    {this.state.open ? <NavigationBack/> : <NevigationExpand/>}
                  </IconButton>
                }>
                <Toolbar>
                  <div style={{color: 'white'}}>
                    <i class="fa fa-user-circle" aria-hidden="true"></i>&nbsp;{this.state.name}
                  </div>
                </Toolbar>
              </AppBar>
              <Drawer open={this.state.open} containerStyle={{'top': '64px'}} width={200}>
                <List>
                  <ListItem button component="a" href="/admin/blue/organizations/jenkins/pipelines">
                    <i className="fa fa-briefcase" aria-hidden="true"> </i><ListItemText primary="Admin Portal"/>
                  </ListItem>
                  <ListItem button component="a" href="/admin">
                    <i className="fa fa-cogs" aria-hidden="true"></i><ListItemText primary="TestGrid Jenkins"/>
                  </ListItem>
                  <ListItem button component="a" href="https://github.com/wso2/testgrid/blob/master/docs/QuickStartGuide.md
">
                    <i className="fa fa-book" aria-hidden="true"></i><ListItemText primary="Quick Start Guide"/>
                  </ListItem>
                  <ListItem button component="a" href="https://github.com/wso2/testgrid-job-configs">
                    <i className="fa fa-wrench" aria-hidden="true"></i><ListItemText primary="TestGrid Job Configs"/>
                  </ListItem>
                </List>
              </Drawer>
              <Paper style={paperStyle} zDepth={2}>
                <Switch>
                  <Route exact path={'/login'} component={Login}/>
                  <Route exact path={'/'} component={ProductContainer}/>
                  <Route exact path={'/:productName'} component={DeploymentContainer}/>
                  <Route exact path={'/:productName/:deploymentPatternName/test-plans/:testPlanId'}
                         component={InfrastructureOverviewContainer}/>
                  <Route exact path={'/scenarios/infrastructure/:infraid'} component={ScenarioContainer}/>
                  <Route exact path={'/testcases/scenario/:scenarioid'} component={TestCaseContainer}/>
                  {/*<Route exact path={'/:productName/:deploymentPatternName/test-plans/:testPlanId'}*/}
                         {/*component={testRunContainer}/>*/}
                </Switch>
              </ Paper>
            </div>
          </MuiThemeProvider>
        </PersistGate>
      </Provider>
    );
  }
}

export default withCookies(App);
