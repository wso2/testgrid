/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import InfrastructureContainer from "../containers/InfrastructureContainer";
import TestRunContainer from "../containers/TestRunContainer";
import {Paper} from "material-ui";
import Grid from '@material-ui/core/Grid';
import {withStyles} from '@material-ui/core/styles';

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  paper: {
    padding: theme.spacing.unit * 2,
    textAlign: 'center',
    color: theme.palette.text.secondary,
  },
});

class InfraCombinationOverview extends Component {

  constructor(props) {
    super(props);
    this.state = {
      direction: 'row',
      justify: 'center',
      alignItems: 'center',
    }
  }

  handleChange = key => (event, value) => {
    this.setState({
      [key]: value,
    });
  };

  render() {
    const { classes } = this.props;

    return (
      <Grid container className={classes.root}>
        <Grid container>
          <Grid
            container
          >
            <Grid item xs={4} sm={3}>
              <Paper
                className={classes.paper}
                style={{paddingTop: 10, paddingBottom: 10}}
              >
                <InfrastructureContainer match={this.props.match}/>
              </Paper>
            </Grid>

            <Grid item xs={12} sm={9}>
              <Paper
                className={classes.paper}
                style={{ paddingTop: 2 * 10, paddingBottom: 2 * 10 }}
              >
                <TestRunContainer match={this.props.match} zDepth={2}/>
              </Paper>
            </Grid>
          </Grid>
        </Grid>
      </Grid>
    );
  }
}

export default withStyles(styles)(InfraCombinationOverview);
