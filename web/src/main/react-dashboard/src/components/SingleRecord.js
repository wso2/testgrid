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
import Moment from 'moment';
import {SUCCESS, ERROR, PENDING, RUNNING, INCOMPLETE, DID_NOT_RUN} from '../constants.js';
import 'font-awesome/css/font-awesome.min.css';
import {Button} from "reactstrap";
import ReactTooltip from 'react-tooltip'

class SingleRecord extends Component {

  render() {
    if (this.props.value === SUCCESS || this.props.value === true) {
      return (
        <div>
          <Button onClick={this.props.nevigate} outline color="success" size="sm" className="success-status-btn">
            <i className="fa fa-check-circle" aria-hidden="true" data-tip="Success!"> </i>
            <ReactTooltip/>
            {(() => {
              if (this.props.time) {
                return (<span className="label"><i> {Moment(this.props.time).fromNow()}</i></span>);
              }
            })()}
          </Button>
        </div>
      )
    } else if (this.props.value === INCOMPLETE) {
      return (
        <div>
          <Button onClick={this.props.nevigate} outline color="info" size="sm" className="incomplete-status-btn">
            <i className="fa fa-hourglass-half" aria-hidden="true" data-tip="Incomplete!"> </i>
            <ReactTooltip/>
            {(() => {
              if (this.props.time) {
                return (<span className="label"><i> {Moment(this.props.time).fromNow()}</i></span>);
              }
            })()}
          </Button>
        </div>
      )
    } else if (this.props.value === DID_NOT_RUN) {
      return (
        <div>
          <Button onClick={this.props.nevigate} outline color="info" size="sm" className="not-run-status-btn">
            <i className="fa fa-ban" aria-hidden="true" data-tip="Did Not Run!"> </i>
            <ReactTooltip/>
            {(() => {
              if (this.props.time) {
                return (<span className="label"><i> {Moment(this.props.time).fromNow()}</i></span>);
              }
            })()}
          </Button>
        </div>
      )
    } else if (this.props.value === ERROR) {
      return (
        <div>
          <Button onClick={this.props.nevigate} outline color="danger" size="sm" className="error-status-btn">
            <i className="fa fa-times-circle" aria-hidden="true" data-tip="Error!"> </i>
            <ReactTooltip/>
            {(() => {
              if (this.props.time) {
                return (<span className="label"><i> {Moment(this.props.time).fromNow()}</i></span>);
              }
            })()}
          </Button>
        </div>
      )
    } else if (this.props.value === RUNNING) {
      return (
        <div>
          <Button onClick={this.props.nevigate} outline color="info" size="sm" className="running-status-btn">
            <i className="fa fa-spinner fa-pulse" data-tip="Running!"> </i>
            <span className="sr-only">Loading...</span>
            <ReactTooltip/>
            {(() => {
              if (this.props.time) {
                return (<span className="label"><i> {Moment(this.props.time).fromNow()}</i></span>);
              }
            })()}
          </Button>
        </div>
      )
    } else if (this.props.value === PENDING) {
      return (
        <div>
          <Button onClick={this.props.nevigate} outline color="info" size="sm">
            <i className="fa fa-tasks" aria-hidden="true" data-tip="Pending!"> </i>
            <ReactTooltip/>
            {(() => {
              if (this.props.time) {
                return (<span className="label"><i> {Moment(this.props.time).fromNow()}</i></span>);
              }
            })()}
          </Button>
        </div>
      )
    }
    else {
      return (
        <div>
          <Button onClick={this.props.nevigate} outline color="danger" size="sm">
            <i className="fa fa-exclamation-circle" aria-hidden="true" data-tip="Failed!"> </i>
            <ReactTooltip/>
            {(() => {
              if (this.props.time) {
                return (<span className="label"><i> {Moment(this.props.time).fromNow()}</i></span>);
              }
            })()}
          </Button>
        </div>
      )
    }
  }
}

export default SingleRecord;
