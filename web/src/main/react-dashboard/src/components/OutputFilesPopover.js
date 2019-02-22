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
import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import Popover from '@material-ui/core/Popover';
import CircularProgress from '@material-ui/core/CircularProgress';
import Snackbar from 'material-ui/Snackbar';
import {HTTP_NOT_FOUND, HTTP_UNAUTHORIZED, LOGIN_URI, TESTGRID_API_CONTEXT} from '../constants.js';
import {HTTP_OK} from "../constants";
import Divider from '@material-ui/core/Divider';

const styles = theme => ({
  typography: {
    margin: theme.spacing.unit * 2,
  },
});

class OutputFilesPopover extends React.Component {
  state = {
    anchorEl: null,
    files: null,
    showLogDownloadErrorDialog: false
  };

  handleClick = event => {
    this.setState({
      anchorEl: event.currentTarget,
    });
    this.getAvailableArchives();
  };

  handleError(response) {
    if (response.status.toString() === HTTP_UNAUTHORIZED) {
      window.location.replace(LOGIN_URI);
    }
    return response;
  }

  toggle(Message) {
    this.setState({
      showLogDownloadStatusMessage: Message,
      showLogDownloadStatusDialog: true,
    });
  }

  handleLogDownloadStatusDialogClose = () => {
    this.setState({
      showLogDownloadStatusDialog: false,
    });
  };

  getAvailableArchives() {
    let url = TESTGRID_API_CONTEXT + '/api/test-plans/archives/list/' + this.props.testPlanId;
    console.log("URL: " + url);
    fetch(url, {
      method: "GET",
      credentials: 'same-origin',
      headers: {
        'Accept': 'application/json'
      }
    }).then(response => {
      if (response.status === HTTP_NOT_FOUND) {
        let errorMessage = "Unable to locate archives in the remote storage.";
        this.toggle(errorMessage);
      } else if (response.status !== HTTP_OK) {
        let errorMessage = "Internal server error. Couldn't download archives at the moment.";
        this.toggle(errorMessage);
      }
      return response;
    })
      .then(this.handleError)
      .then(response => { return response.json();})
      .then(responseData => {console.log(responseData);
        this.setState({files: responseData});
      })
      .catch(error => console.error(error));
  }

  downloadArchive(testPlanId, file) {
    let url = TESTGRID_API_CONTEXT + '/api/test-plans/archives/' + testPlanId + "/" + file;
    console.log("URL for request file: " + url);
    fetch(url, {
      method: "GET",
      credentials: 'same-origin',
    }).then(response => {
        if (response.status === HTTP_NOT_FOUND) {
          let errorMessage = "Unable to locate results in the remote storage.";
          this.toggle(errorMessage);
        } else if (response.status !== HTTP_OK) {
          let errorMessage = "Internal server error. Couldn't download the results at the moment.";
          this.toggle(errorMessage);
        } else if (response.status === HTTP_OK) {
          let statusMessage = "Download will begin in a moment..";
          this.toggle(statusMessage);
          document.location = url;
        }
      }
    ).catch(error => console.error(error));
  };

  handleClose = () => {
    this.setState({
      anchorEl: null,
    });
  };

  downloadTestOutputs() {
    let url = TESTGRID_API_CONTEXT + '/api/test-plans/result/' + this.props.testPlanId;
    fetch(url, {
      method: "GET",
      credentials: 'same-origin',
    }).then(response => {
        if (response.status === HTTP_NOT_FOUND) {
          let errorMessage = "Unable to locate results in the remote storage.";
          this.toggle(errorMessage);
        } else if (response.status !== HTTP_OK) {
          let errorMessage = "Internal server error. Couldn't download the results at the moment.";
          this.toggle(errorMessage);
        } else if (response.status === HTTP_OK) {
          let statusMessage = "Download will begin in a moment..";
          this.toggle(statusMessage);
          document.location = url;
        }
      }
    ).catch(error => console.error(error));
  }

  render() {
    const { classes } = this.props;
    const { anchorEl } = this.state;
    const open = Boolean(anchorEl);

    return (
      <div style={{"padding-left": "10px"}}>
        <Snackbar
          open={this.state.showLogDownloadStatusDialog}
          message={this.state.showLogDownloadStatusMessage}
          autoHideDuration={4000}
          onRequestClose={this.handleLogDownloadStatusDialogClose}
          contentStyle={{
            fontWeight: 600,
            fontSize: "15px"
          }}
        />
        <Button
          aria-owns={open ? 'simple-popper' : undefined}
          aria-haspopup="true"
          variant="contained"
          onClick={this.handleClick}
        >
          <i className="fa fa-download" aria-hidden="true"> </i>  &nbsp;Downloads
        </Button>
        <Popover
          id="simple-popper"
          open={open}
          anchorEl={anchorEl}
          onClose={this.handleClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'center',
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'center',
          }}
        >
          <Typography className={classes.typography}>
            <div>
              <p align={"center"}><b>Tests Outputs</b></p>
              <Button id ="{data}" onClick={this.downloadTestOutputs.bind(this)}>
                <i className="fa fa-download" aria-hidden="true"> </i> &nbsp;Test results and logs</Button>
              <br/>
              <Divider/>
              <br/>
              <p align={"center"}><b>Deployment Outputs</b></p>
              {this.state.files && this.state.files.map((data) => {
                return <div><Button id ="{data}" onClick={()=>{this.downloadArchive(this.props.testPlanId, data)}}><i className="fa fa-download" aria-hidden="true"> </i> &nbsp;{data}</Button></div>
              })
              }
              {(() => {
                if (this.state.files === null) {
                  return(
                    <div align={"center"}>
                      Searching in the storage..
                      <br/>
                      <br/>
                      <CircularProgress style={{margin: "auto"}} className={classes.progress} />
                    </div>);
                } else if (this.state.files.length === 0) {
                  return <div align={"center"}> No deployment outputs found <span class="fa fa-frown-o"></span></div>
                }
              })()}
            </div>
          </Typography>
        </Popover>
      </div>
    );
  }
}

OutputFilesPopover.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(OutputFilesPopover);