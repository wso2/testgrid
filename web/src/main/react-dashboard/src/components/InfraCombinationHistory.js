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
import Moment from 'moment';
import {HTTP_UNAUTHORIZED, LOGIN_URI, TESTGRID_API_CONTEXT, TESTGRID_CONTEXT} from '../constants.js';
import {Button, Card, CardText, Table} from 'reactstrap';
import IconButton from '@material-ui/core/IconButton';
import DeleteIcon from '@material-ui/icons/Delete';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';

class InfraCombinationHistory extends Component {

  constructor(props) {
    super(props);
    this.state = {
      hits: [],
      currentInfra: null,
      open: false
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

  static parseInfraCombination(infraCombination){
    let infraData = JSON.parse(infraCombination);

    return <Card body inverse>
      <CardText>
        {
          Object.entries(infraData).sort().map(([key, value], index) => {

            return (
              <span key={index}>
                <small className="text-dark" key={key} style={{paddingRight: "20px"}}><b>{key}</b>: {value}</small>
                {index % 3 === 2 ? <br/> : ""}
              </span>
          )
        })
        }
      </CardText>
    </Card>
  }

  navigateToRoute(route, deployment, testPlan) {
    this.props.dispatch(add_current_deployment(deployment));
    this.props.dispatch(add_current_infra(testPlan));
    this.props.history.push(route);
  }

  handleDeleteDialogOpen = (id) => {
    this.setState({ open: true, testPlanToDelete:  id});
  };

  handleDelete = () => {
    let url = TESTGRID_API_CONTEXT + "/api/test-plans/" + this.state.testPlanToDelete;
    fetch(url, {
      method: "DELETE",
      credentials: 'same-origin',
    })
      .then(this.handleError)
      .then(() => {
        this.setState(prevState => ({
          open: false,
          testPlanToDelete: "",
          hits: prevState.hits.filter(hit => hit.id !== prevState.testPlanToDelete)
        }));
      }).catch(error => console.error(error));
  };

  handleClose = () => {
    this.setState({ open: false });
  };

  componentDidMount() {
    let url = TESTGRID_API_CONTEXT + "/api/test-plans/history/" + this.props.match.params.testPlanId;
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
        currentInfra = {};
        currentInfra.infraParameters = data[0].infraParams;
        currentInfra.relatedProduct = this.props.match.params.productName;
        currentInfra.relatedDeplymentPattern = this.props.match.params.deploymentPatternName;
        this.setState({hits: data, currentInfra: currentInfra});
      })
      .catch(error => console.error(error));
  }

  render() {
    let {productName, deploymentPatternName} = this.props.match.params;
    return (
      <div>
        <b style={{'paddingLeft': '10px'}}>Build History</b>
        <Table responsive hover fixedheader="false">
          <thead displayselectall="false" adjustforcheckbox="false">
          <tr>
            <th>#</th>
            <th>Status</th>
            <th>Date</th>
          </tr>
          </thead>
          <tbody displayrowcheckbox="false">

          {this.state.hits
            .sort((a, b) => b.createdTimestamp - a.createdTimestamp)
            .map((data, index) => {
              return (
                <tr key={data.id} id={data.id}
                     style={{height: 'inherit', width: '100%', 'maxWidth': '150px', cursor: 'pointer'}}
                     onClick={() => this.navigateToRoute(TESTGRID_CONTEXT + "/" +
                       productName + "/" +
                       deploymentPatternName + "/test-plans/"
                       + data.id, {
                       deploymentPatternName:
                       this.state.currentInfra.relatedProduct
                     }, {
                       testPlanId: data.id,
                       infraParameters: data.infraParams,
                       testPlanStatus: data.status
                     })}>
                <td>{data.testRunNumber}</td>
                <td>
                    <SingleRecord value={data.status}/>
                </td>
                <td>{Moment(data.createdTimestamp).calendar()}</td>
                <td onClick={false}>
                  <IconButton aria-label="Delete" onClick={this.handleDeleteDialogOpen.bind(this, data.id)}>
                    <DeleteIcon fontSize="small" />
                  </IconButton>

                </td>
              </tr>
 )
            })}
          </tbody>
        </Table>
        <Dialog
          open={this.state.open}
          onClose={this.handleClose}
          aria-labelledby="alert-dialog-title"
          aria-describedby="alert-dialog-description"
        >
          <DialogTitle id="alert-dialog-title">{"Delete test run?"}</DialogTitle>
          <DialogContent>
            <DialogContentText id="alert-dialog-description">
              Delete {this.state.testPlanToDelete}?
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={this.handleDelete.bind(this, this.state.testPlanToDelete)}>
              Yes
            </Button>
            <Button onClick={this.handleClose} autoFocus>
              No
            </Button>
          </DialogActions>
        </Dialog>

      </div>
    );
  }
}

export default InfraCombinationHistory;
