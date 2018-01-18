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

class SingleRecord extends Component {
  constructor(props) {
    super(props);
    this.state = {
      open: false
    };
  }

  render() {
    if (this.props.value === 'SUCCESS' || this.props.value=== true) {
      return (
        <div style={{"fontSize": "16px"}}>
          <img src={require('../success.png')} width="28" height="28" onClick={this.props.nevigate} style={{ cursor: 'pointer' }} alt='Succesful' />
          <i> {this.props.time}</i>
        </div>
      )
    } else {
      return (
        <div style={{"fontSize": "16px",cursor:"pointer"}}>
        <img src={require('../close.png')} width="28" height="28" onClick={this.props.nevigate} style={{ cursor: 'pointer' }} alt='Failed' />
        <i> {this.props.time}</i>
        </div>
      )
    }
  }
}

export default SingleRecord