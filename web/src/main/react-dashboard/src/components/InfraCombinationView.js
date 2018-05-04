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
import '../App.css';
import Subheader from 'material-ui/Subheader';
import SingleRecord from './SingleRecord.js';
import {add_current_infra, add_current_deployment} from '../actions/testGridActions.js';
import FlatButton from 'material-ui/FlatButton';
import Divider from 'material-ui/Divider';
import Moment from 'moment';
import {FAIL, SUCCESS, ERROR, PENDING, RUNNING, HTTP_UNAUTHORIZED, LOGIN_URI, TESTGRID_CONTEXT, DID_NOT_RUN, INCOMPLETE}
  from '../constants.js';
import {Table} from 'reactstrap';

class InfraCombinationView extends Component {

  constructor(props) {
    super(props);
    this.state = {
      hits: [],
      currentInfra: null
    }
  }

  handleError(response) {
    if (response.status.toString() === HTTP_UNAUTHORIZED) {
      window.location.replace(LOGIN_URI);
    } else if (!response.ok) {
      throw Error(response.statusText)
    }
    return response;
  }

  navigateToRoute(route, deployment, testPlan) {
    this.props.dispatch(add_current_deployment(deployment));
    this.props.dispatch(add_current_infra(testPlan));
    this.props.history.push(route);
  }

  componentDidMount() {
    let currentUrl = window.location.href.split("/");
    let url = TESTGRID_CONTEXT + "/api/test-plans/history/" + currentUrl[currentUrl.length - 2];
    let currentInfra;
    fetch(url, {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        'Accept': 'application/json'
      }
    })
      .then(this.handleError)
      .then(response => {
        return response.json()
      })
      .then(data => {
        let currentUrl = window.location.href.split("/");
        if (this.props.active.reducer.currentInfra) {
          currentInfra = this.props.active.reducer.currentInfra;
        } else {
          currentInfra = {};
          currentInfra.testPlanId = data[0].id;
          currentInfra.infraParameters = data[0].infraParams;
          currentInfra.testPlanStatus = data[0].status;
          this.props.active.reducer.currentInfra = currentInfra;
        }
        currentInfra.relatedProduct = currentUrl[currentUrl.length - 4];
        currentInfra.relatedDeplymentPattern = currentUrl[currentUrl.length - 3];
        this.setState({hits: data, currentInfra: currentInfra});
      })
      .catch(error => console.error(error));
  }

  render() {
    return (
      <div>
        {this.state && this.state.currentInfra && (() => {
          switch (this.state.currentInfra.testPlanStatus) {
            case FAIL:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#ffd6d3"
              }}>
                <Table responsive>
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../close.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i>{this.state.currentInfra.relatedProduct} /
                      {this.state.currentInfra.relatedDeplymentPattern} <br/>
                      {this.state.currentInfra.infraParameters}
                    </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case SUCCESS:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#cdffba"
              }}>
                <Table responsive>
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../success.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i>{this.state.currentInfra.relatedProduct} /
                      {this.state.currentInfra.relatedDeplymentPattern} <br/>
                      {this.state.currentInfra.infraParameters}
                    </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case PENDING:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#cdffba"
              }}>
                <Table responsive>
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../new.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i>{this.state.currentInfra.relatedProduct} /
                      {this.state.currentInfra.relatedDeplymentPattern} <br/>
                      {this.state.currentInfra.infraParameters}
                    </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case DID_NOT_RUN:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#cdffba"
              }}>
                <Table>
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../did_not_run.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i>{this.state.currentInfra.relatedProduct} /
                      {this.state.currentInfra.relatedDeplymentPattern} <br/>
                      {this.state.currentInfra.infraParameters}
                    </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case INCOMPLETE:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#cdffba"
              }}>
                <Table responsive>
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../incomplete.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i>{this.state.currentInfra.relatedProduct} /
                      {this.state.currentInfra.relatedDeplymentPattern} <br/>
                      {this.state.currentInfra.infraParameters}
                    </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case ERROR:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#ffd6d3"
              }}>
                <Table responsive>
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../error.png')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i>{this.state.currentInfra.relatedProduct} /
                      {this.state.currentInfra.relatedDeplymentPattern} <br/>
                      {this.state.currentInfra.infraParameters}
                    </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
            case RUNNING:
            default:
              return <Subheader style={{
                fontSize: '20px',
                backgroundColor: "#FFCC80"
              }}>
                <Table responsive>
                  <tbody>
                  <tr>
                    <td style={{padding: 5}}>
                      <img
                        src={require('../wait.gif')}
                        style={{
                          verticalAlign: "middle",
                          height: "50px",
                          width: "50px"
                        }}/>
                    </td>
                    <i>{this.state.currentInfra.relatedProduct} /
                      {this.state.currentInfra.relatedDeplymentPattern} <br/>
                      {this.state.currentInfra.infraParameters}
                    </i>
                  </tr>
                  </tbody>
                </Table>
              </Subheader>;
          }
        })()}
        <Divider/>
        <Table responsive fixedHeader={false}>
          <thead displaySelectAll={false} adjustForCheckbox={false}>
          <tr>
            <th>#</th>
            <th>Status</th>
            <th>Date</th>
            <th>Duration</th>
          </tr>
          </thead>
          <tbody displayRowCheckbox={false}>

          {this.state.hits
            .sort((a, b) => b.createdTimestamp - a.createdTimestamp)
            .map((data, index) => {
              return (<tr key={index}>
                <td>{index + 1}</td>
                <td>
                  <FlatButton style={{color: '#0E457C'}}
                              onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                                this.state.currentInfra.relatedProduct + "/" +
                                this.state.currentInfra.relatedDeplymentPattern + "/test-plans/"
                                + data.id, {
                                deploymentPatternName:
                                this.state.currentInfra.relatedProduct
                              }, {
                                testPlanId: data.id,
                                infraParameters: data.infraParams,
                                testPlanStatus: data.status
                              })}>
                    <SingleRecord value={data.status}/>
                  </FlatButton>
                </td>
                <td>{Moment(data.createdTimestamp).calendar()}</td>
                <td>
                  {(() => {
                    let start = Moment(data.createdTimestamp);
                    let end = Moment(data.modifiedTimestamp);
                    let min = end.diff(start, 'minutes');
                    let hours = Math.floor(min / 60);
                    let minutes = min % 60;
                    if (hours > 0) {
                      return (
                        <p><b> {hours} </b> Hours <b> {minutes} </b> Minutes </p>
                      )
                    } else {
                      return (
                        <p><b> {minutes} </b> Minutes </p>
                      )
                    }
                  })()}
                </td>
              </tr>)
            })}
          </tbody>
        </Table>
      </div>
    );
  }
}

export default InfraCombinationView;
