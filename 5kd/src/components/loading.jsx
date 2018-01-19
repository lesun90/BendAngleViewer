// Loading screen view, warns when no BLE adapter is available.
import React from 'react';
import SingleColumnView from './singlecolumnview.js';


export default class Loading extends React.Component {
  componentDidMount() {
    // Start with error message hidden, then wait 5 seconds to display it.
    // This gives some time for the adapter to power on and the page to be
    // changed to scanning (by main.js).
    $('#load-error').hide();
    setTimeout(function() {
      $('#load-error').show();
    }, 5000);
  }

  render() {
    return (
      <SingleColumnView header='Loading...'>
        <div id='load-error'>
          <h4>DMI True Degrees has not powered on!</h4>
          <p>Check the following to resolve the issue:</p>
          <ul>
            <li>Make sure you turn on the device or check the device's battery</li>
            <li>Make sure you're using a CSR8510 USB Bluetooth 4.0
            adapter.  You must also make sure the adapter has been changed to use
            the WinUSB driver using Zadig tool.  See the instructions in Readme file</li>
          </ul>
        </div>
      </SingleColumnView>
    );
  }
}
