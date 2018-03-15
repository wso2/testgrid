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
import Moment from 'moment';
import {FAIL,SUCCESS,ERROR,PENDING,RUNNING } from '../constants.js';

class SingleRecord extends Component {
  constructor(props) {
    super(props);
  }

  render() {
    if (this.props.value === SUCCESS || this.props.value === true) {
      return (
        <div style={{ "fontSize": "16px" }}>
          <img src={require('../success.png')} width="28" height="28"
            onClick={this.props.nevigate}
            style={{ cursor: 'pointer' }}
            alt='Succesful' />
          {(() => {
            if (this.props.time) {
              return (<i> {Moment(this.props.time).fromNow()}</i>);
            }
          })()}
        </div>
      )
    } else if (this.props.value === RUNNING) {
      return (
        <div style={{ "fontSize": "16px" }}>
        <img src={require('../wait.gif')} width="30" height="30"
          onClick={this.props.nevigate}
          style={{ cursor: 'pointer' }}
          alt='Running' />
        {(() => {
          if (this.props.time) {
            return (<i> {Moment(this.props.time).fromNow()}</i>);
          }
        })()}
      </div>
      )
    } else if (this.props.value === PENDING) {
        return (
            <div style={{ "fontSize": "16px" }}>
              <img src={require('../new.png')} width="30" height="30"
                   onClick={this.props.nevigate}
                   style={{ cursor: 'pointer' }}
                   alt='Pending' />
                {(() => {
                    if (this.props.time) {
                        return (<i> {Moment(this.props.time).fromNow()}</i>);
                    }
                })()}
            </div>
        )
    }
    else {
      return (
        <div style={{ "fontSize": "16px", cursor: "pointer" }}>
          <img src={require('../close.png')}
            width="28" height="28"
            onClick={this.props.nevigate}
            style={{ cursor: 'pointer' }} alt='Failed' />
          {(() => {
            if (this.props.time) {
              return (<i> {Moment(this.props.time).fromNow()}</i>);
            }
          })()}
        </div>
      )
    }
  }
}

export default SingleRecord