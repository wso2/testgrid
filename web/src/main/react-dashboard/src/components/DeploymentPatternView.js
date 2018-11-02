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
import SingleRecord from './SingleRecord.js';
import {add_current_deployment, add_current_infra} from '../actions/testGridActions.js';
import ReactTooltip from 'react-tooltip'
import FlatButton from 'material-ui/FlatButton';
import {HTTP_UNAUTHORIZED, LOGIN_URI, TESTGRID_CONTEXT} from '../constants.js';
import {Table, Input, Alert} from 'reactstrap';
import Moment from "moment/moment";

class DeploymentPatternView extends Component {

  constructor(props) {
    super(props);
    this.state = {
      hits: [],
      hitsClone: [],
      product: null
    };
  }

  handleError(response) {
    if (response.status.toString() === HTTP_UNAUTHORIZED) {
      window.location.replace(LOGIN_URI);
    } else if (!response.ok) {
      throw Error(response.statusText)
    }
    return response;
  }

  handleInputChange = () => {
    let hits = this.state.hitsClone;
    if (this.search.value.length > 0) {
      let searchHits = hits.filter(hit => hit.lastBuild.infraParams.toLowerCase().includes(this.search.value.toLowerCase()));
      this.setState({
        hits: searchHits
      })
    } else {
      this.setState({
        hits: this.state.hitsClone
      })
    }
  };

  componentDidMount() {
    let currentProduct = this.props.active.reducer.currentProduct;
    if (!currentProduct) {
      let url = TESTGRID_CONTEXT + '/api/products/product-status/' + window.location.href.split("/").pop();
      fetch(url, {
        method: "GET",
        credentials: 'same-origin',
        headers: {
          'Accept': 'application/json'
        }
      }).then(this.handleError)
        .then(response => {
          return response.json();
        })
        .then(data => {
          currentProduct = data;
          return currentProduct;
        })
        .then(currentProduct => this.getDeploymentDetails(currentProduct))
        .catch(error => console.error(error));
    } else {
      this.getDeploymentDetails(currentProduct);
    }
  }

  getDeploymentDetails(currentProduct) {
    let url = TESTGRID_CONTEXT + '/api/test-plans/product/' + currentProduct.productId;
    fetch(url, {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        'Accept': 'application/json'
      }
    }).then(this.handleError)
      .then(response => {
        return response.json()
      })
      .then(data => this.setState({hits: data, hitsClone: data, product: currentProduct}))
      .catch(error => console.error(error));
  }

  navigateToRoute(route, deployment, testPlan) {
    this.props.dispatch(add_current_deployment(deployment));
    this.props.dispatch(add_current_infra(testPlan));
    this.props.history.push(route);
  }

  render() {
    const FAIL = "FAIL";
    const ERROR = "ERROR";
    const SUCCESS = "SUCCESS";
    const RUNNING = "RUNNING";
    const PENDING = "PENDING";
    const INCOMPLETE = "INCOMPLETE";
    const DID_NOT_RUN = "DID_NOT_RUN";

    var groupByDeployment = {};
    this.state.hits.forEach((value, index) => {
      if (groupByDeployment[value.lastBuild.deploymentPattern] === undefined) {
        groupByDeployment[value.lastBuild.deploymentPattern] = [{
          'lastBuild': value.lastBuild, 'lastFailed':
          value.lastFailure
        }]
      } else {
        groupByDeployment[value.lastBuild.deploymentPattern].push({
          'lastBuild': value.lastBuild, 'lastFailed':
          value.lastFailure
        })
      }
    });
    return (
      <div>
        {this.state && this.state.product && (() => {
          switch (this.state.product.productStatus) {
            case FAIL:
              return <Alert color="danger">
                <i className="fa fa-exclamation-circle" aria-hidden="true" data-tip="Failed!"> </i>
                <ReactTooltip/>
                {" " + this.state.product.productName}
              </Alert>;
            case SUCCESS :
              return <Alert color="success">
                <i className="fa fa-check-circle" aria-hidden="true" data-tip="Success!"> </i>
                <ReactTooltip/>
                {" " + this.state.product.productName}
              </Alert>;
            case ERROR :
              return <Alert color="danger">
                <i className="fa fa-times-circle" aria-hidden="true" data-tip="Error!"> </i>
                <ReactTooltip/>
                {" " + this.state.product.productName}
              </Alert>;
            case INCOMPLETE :
              return <Alert color="info">
                <i className="fa fa-hourglass-half" aria-hidden="true" data-tip="Incomplete!"> </i>
                <ReactTooltip/>
                {" " + this.state.product.productName}
              </Alert>;
            case DID_NOT_RUN :
              return <Alert color="info">
                <i className="fa fa-ban" aria-hidden="true" data-tip="Did Not Run!"> </i>
                <ReactTooltip/>
                {" " + this.state.product.productName}
              </Alert>;
            case PENDING:
              return <Alert color="info">
                <i className="fa fa-tasks" aria-hidden="true" data-tip="Pending!"> </i>
                <ReactTooltip/>
                {" " + this.state.product.productName}
              </Alert>;
            case RUNNING:
            default:
              return <Alert color="info">
                <i className="fa fa-spinner fa-pulse" data-tip="Running!"> </i>
                <span className="sr-only">Loading...</span>
                <ReactTooltip/>
                {" " + this.state.product.productName}
              </Alert>;
          }
        })()}
        <div>
          <Input placeholder="Search Infra..." innerRef={Input => this.search = Input} onChange={this.handleInputChange}/>
        </div>
        <Table responsive bordered className='deployment-pattern-view' fixedHeader={false} size="sm">
          <thead displaySelectAll={false} adjustForCheckbox={false}>
          <tr style={{borderBottom: '0'}}>
            <th rowSpan='2' className="text-center">Deployment Pattern</th>
            <th className="text-center">Infra Combination</th>
            <th rowSpan='2' className="text-center">Last Build</th>
            <th rowSpan='2' className="text-center">Last Failure</th>
          </tr>
          <tr class='infra-param-header'>
            <td>
              <p className="text-center">OS</p>
              <p className="text-center">Database</p>
              <p className="text-center">JDK</p>
            </td>
          </tr>
          </thead>
          <tbody displayRowCheckbox={false}>
          {this.state && this.state.product && Object.keys(groupByDeployment).map((key) => {
            return (
              groupByDeployment[key].map((value, index) => {
                var infraParameters = JSON.parse(value.lastBuild.infraParams);
                if (index === 0) {
                  return (
                    <tr>
                      <td class='deployment-pattern-name'
                          rowSpan={groupByDeployment[key].length}>{key}</td>
                      <td style={{whiteSpace: 'normal', wordWrap: 'break-word'}}>
                        <FlatButton class='view-history' data-tip="View History"
                                    onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                                      this.state.product.productName + "/" + key + "/" +
                                      value.lastBuild.id +
                                      "/infra", {deploymentPatternName: key}, {
                                      testPlanId: value.lastBuild.id,
                                      infraParameters: value.lastBuild.infraParams,
                                      testPlanStatus: value.lastBuild.status
                                    })}>
                          <p class='infra-param'>
                            {infraParameters.OS} {infraParameters.OSVersion}
                          </p>
                          <p class='infra-param'>
                            {infraParameters.DBEngine} {infraParameters.DBEngineVersion}
                          </p>
                          <p class='infra-param'>
                            {infraParameters.JDK}
                          </p>
                        </FlatButton>
                        <ReactTooltip/>
                      </td>
                      <td>
                        <FlatButton style={{height: 'inherit', width: '100%', maxWidth: '150px'}}
                          onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                            this.state.product.productName + "/" + key + "/test-plans/" +
                            value.lastBuild.id, {
                            deploymentPatternName: key
                          }, {
                            testPlanId: value.lastBuild.id,
                            infraParameters: value.lastBuild.infraParams,
                            testPlanStatus: value.lastBuild.status
                          })}>
                          <SingleRecord value={value.lastBuild.status}
                                        time={value.lastBuild.modifiedTimestamp}/>
                        </FlatButton>
                      </td>
                      <td style={{fontSize: '16px'}}>
                        {(() => {
                          if (value.lastFailed.modifiedTimestamp) {
                            return (
                              <i onClick={() => this.navigateToRoute(TESTGRID_CONTEXT +
                                "/" + this.state.product.productName + "/" + key +
                                "/test-plans/" + value.lastFailed.id,
                                {deploymentPatternName: key}, {
                                  testPlanId: value.lastFailed.id,
                                  infraParameters: value.lastFailed.infraParams,
                                  testPlanStatus: value.lastFailed.status
                                })}
                                 style={{cursor: 'pointer'}}>{Moment(value.lastFailed.modifiedTimestamp).fromNow()}</i>
                            );
                          } else {
                            return (
                              <FlatButton
                                style={{fontSize: '16px'}}>No failed builds yet!
                              </FlatButton>
                            )
                          }
                        })()}
                      </td>
                      {/* Note: Commented until the backend coordination is configured.
                        <td>
                        <Button outline color="info" size="sm" onClick={() => {
                          window.location =
                            '/admin/job/' + this.state.product.productName + '/build'
                        }}>
                          <i className="fa fa-play-circle" aria-hidden="true"> </i>
                        </Button>
                        <ReactTooltip/></td>*/}
                    </tr>
                  )
                } else {
                  return (
                    <tr>
                      <td style={{whiteSpace: 'normal', wordWrap: 'break-word'}}>
                        <FlatButton class='view-history' data-tip="View History"
                                    onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                                      this.state.product.productName + "/" + key + "/" +
                                      value.lastBuild.id + "/infra",
                                      {deploymentPatternName: key}, {
                                        testPlanId: value.lastBuild.id,
                                        infraParameters: value.lastBuild.infraParams,
                                        testPlanStatus: value.lastBuild.status
                                      })}>
                          <p class='infra-param'>
                            {infraParameters.OS} {infraParameters.OSVersion}
                          </p>
                          <p class='infra-param'>
                            {infraParameters.DBEngine} {infraParameters.DBEngineVersion}
                          </p>
                          <p class='infra-param'>
                            {infraParameters.JDK}
                          </p>
                        </FlatButton>
                        <ReactTooltip/>
                      </td>
                      <td>
                        <FlatButton style={{height: 'inherit', width: '100%', maxWidth: '150px'}}
                          onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                            this.state.product.productName + "/" + key + "/test-plans/" +
                            value.lastFailed.id, {
                            deploymentPatternName: key
                          }, {
                            testPlanId: value.lastBuild.id,
                            infraParameters: value.lastBuild.infraParams,
                            testPlanStatus: value.lastBuild.status
                          })}>
                          <SingleRecord value={value.lastBuild.status}
                                        time={value.lastBuild.modifiedTimestamp}/>
                        </FlatButton>
                      </td>
                      <td style={{fontSize: '16px'}}>
                        {(() => {
                          if (value.lastFailed.modifiedTimestamp) {
                            return (
                              <i onClick={() => this.navigateToRoute(TESTGRID_CONTEXT +
                                "/" + this.state.product.productName + "/" + key +
                                "/test-plans/" + value.lastFailed.id,
                                {deploymentPatternName: key}, {
                                  testPlanId: value.lastFailed.id,
                                  infraParameters: value.lastFailed.infraParams,
                                  testPlanStatus: value.lastFailed.status
                                })}
                                 style={{cursor: 'pointer'}}>{Moment(value.lastFailed.modifiedTimestamp).fromNow()}</i>
                            );
                          } else {
                            return (
                              <FlatButton style={{fontSize: '16px'}}>
                                No failed builds yet!
                              </FlatButton>
                            )
                          }
                        })()}
                      </td>
                      {/* Note: Commented until the backend coordination is configured.
                        <td>
                        <Button outline color="info" size="sm" onClick={() => {
                          window.location =
                            '/admin/job/' + this.state.product.productName + '/build'
                        }}>
                          <i className="fa fa-play-circle" aria-hidden="true"> </i>
                        </Button>
                      </td> */}
                    </tr>
                  )
                }
              })
            )
          })}
          </tbody>
        </Table>
      </div>
    );
  }
}

export default DeploymentPatternView;
