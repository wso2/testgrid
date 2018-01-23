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
import '../App.css';
import {
  Table,
  TableBody,
  TableHeader,
  TableHeaderColumn,
  TableRow,
  TableRowColumn,
} from 'material-ui/Table';
import Subheader from 'material-ui/Subheader';
import SingleRecord from './SingleRecord.js';
import {add_current_deployment, add_current_infra} from '../actions/testGridActions.js';
import Moment from 'moment'
import ReactTooltip from 'react-tooltip'
import FlatButton from 'material-ui/FlatButton';

class DeploymentPatternView extends Component {

  constructor(props) {
    super(props)
    this.state = {
      hits: []
    };
  }

  componentDidMount() {
     var url = '/testgrid/v0.9/api/test-plans/product/' + this.props.active.reducer.currentProduct.productId;
    fetch(url, {
      method: "GET",
      headers: {
        'Accept': 'application/json'
      }
    }).then(response => {
      if (response.ok) {
        return response.json();
      }
    })
      .then(data => this.setState({ hits: data }));
  }

  navigateToRoute(route, deployment, testPlan) {
    this.props.dispatch(add_current_deployment(deployment));
    this.props.dispatch(add_current_infra(testPlan));
    this.props.history.push(route);
  }

  render() {
    var x = {};
    this.state.hits.map((value, index) => {
      if (x[value.lastBuild.deploymentPattern] == undefined) {
        x[value.lastBuild.deploymentPattern] = [{ 'lastBuild': value.lastBuild, 'lastFailed': value.lastFailure }]
      } else {
        x[value.lastBuild.deploymentPattern].push({ 'lastBuild': value.lastBuild, 'lastFailed': value.lastFailure })
      }
    })
    return (
      <div>
        <Subheader style={{ fontSize: '20px' }} > <i> {this.props.active.reducer.currentProduct.productName + " "} </i> </Subheader>
        <Table fixedHeader={false} style={{ tableLayout: 'auto' }}>
          <TableHeader displaySelectAll={false} adjustForCheckbox={false} >
            <TableRow>
              <TableHeaderColumn ><h2>Deployment Pattern</h2></TableHeaderColumn>
              <TableHeaderColumn><h2>Infra Combination</h2></TableHeaderColumn>
              <TableHeaderColumn><h2>Last Build</h2></TableHeaderColumn>
              <TableHeaderColumn><h2>Last Failure</h2></TableHeaderColumn>
              <TableHeaderColumn><h2>Execute</h2></TableHeaderColumn>
            </TableRow>
          </TableHeader>
          <TableBody displayRowCheckbox={false}>
            {Object.keys(x).map((key) => {
              return (
                x[key].map((value, index) => {
                  if (index == 0) {
                    return (
                      <TableRow>
                        <TableRowColumn rowSpan={x[key].length}>{key}</TableRowColumn>
                        <TableRowColumn style={{ whiteSpace: 'normal', wordWrap: 'break-word' }}>{value.lastBuild.infraParams}</TableRowColumn>
                        <TableRowColumn>
                          <FlatButton style={{color: '#0E457C'}}
                                      onClick={() => this.navigateToRoute("/testgrid/v0.9/testplans/" + value.lastBuild.id, {
                                        deploymentName: key
                                      }, {
                                        testPlanId: value.lastBuild.id,
                                        infraParameters: value.lastBuild.infraParams,
                                        testPlanStatus: value.lastBuild.status
                                      })}>
                            <SingleRecord value={value.lastBuild.status}
                                          time={Moment(value.lastBuild.modifiedTimestamp).fromNow()}/>
                          </FlatButton>
                        </TableRowColumn>
                        <TableRowColumn>
                          <FlatButton style={{color: '#0E457C'}}
                                      onClick={() => this.navigateToRoute("/testgrid/v0.9/testplans/" + value.lastFailed.id, {
                                        deploymentName: key
                                      }, {
                                        testPlanId: value.lastFailed.id,
                                        infraParameters: value.lastFailed.infraParams,
                                        testPlanStatus: value.lastFailed.status
                                      })}>
                            <SingleRecord value={value.lastFailed.status}
                                          time={Moment(value.lastFailed.modifiedTimestamp).fromNow()}
                            />
                          </FlatButton>
                        </TableRowColumn>
                        <TableRowColumn> <img  src={require('../play.png')} width="48" height="48" data-tip="Execute Job" onClick={() => { window.location = '/job/wso2is5.4.0LTS/build' }}/><ReactTooltip /></TableRowColumn>
                      </TableRow>

                    )
                  } else {
                    return (

                      <TableRow>
                        <TableRowColumn >{value.lastBuild.infraParams}</TableRowColumn>
                        <TableRowColumn ><SingleRecord value={value.lastBuild.status}
                             time={Moment(value.lastBuild.modifiedTimestamp).fromNow()}
                        /> </TableRowColumn>
                        <TableRowColumn ><SingleRecord value={value.lastFailed.status}
                           time={Moment(value.lastFailed.modifiedTimestamp).fromNow()}
                        /> </TableRowColumn>
                        <TableRowColumn> <img src={require('../play.png')} width="48" height="48" data-tip="Execute Job"  onClick={() => { window.location = '/job/wso2is5.4.0LTS/build' }}/><ReactTooltip /></TableRowColumn>
                      </TableRow>
                    )
                  }
                })
              )
            })}
          </TableBody>
        </Table>
      </div>
    );
  }
}

export default DeploymentPatternView;