// View for the about state.
import React from 'react';
import ipc from 'ipc';
import able from '../../package.json';
import noble from 'noble/package.json';


export default class About extends React.Component {
  constructor() {
    super();
  }

  render(){
    // Render about view.
    // TODO: Make the links open in the default browser.  Right now there are
    // major issues with opening in an external borwser with electron 0.30.4
    // and Linux.  When these are resolved make the list below into real links.
    return (
      <div className='modal fade' id='about-modal' tabIndex='-1' role='dialog'>
        <div className='modal-dialog' role='document'>
          <div className='modal-content'>
            <div className='modal-header'>
              <button type='button' className='close' data-dismiss='modal' aria-label='Close'><span aria-hidden='true'>&times;</span></button>
              <h4 className='modal-title'>HELP</h4>
            </div>
            <div className='modal-body'>
              <p>Please read the Readme.pdf for the instructions of how to to use</p>
            </div>
            <div className='modal-footer'>
              <button type='button' className='btn btn-default' data-dismiss='modal'>Close</button>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
