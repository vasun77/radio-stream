import loggerCreator from '../utils/logger'
var moduleLogger = loggerCreator("backend_metadata_api");

const btoa = require('base-64').encode;
import Ajax from '../utils/ajax'
import { globalSettings } from '../utils/settings'

class BackendMetadataApi {

  // NOTE: Since the host might change, we create a new Ajax object every time
  _getAjax(customHost, customPassword) {
    let logger = loggerCreator("_getAjax", moduleLogger);
    logger.info(`start`);

    let host = globalSettings.host;
    let password = globalSettings.password;

    if (customHost && customPassword) {
      logger.info(`using custom host/password`);
      host = customHost;
      password = customPassword;
    } else {
      logger.info(`using global host/password`);
    }

    if(!host || !password) {
      throw "host or password are empty"
    }

    const address = `http://${host}/radio-stream/api`;
    const credentials = btoa(unescape(encodeURIComponent(globalSettings.user + ':' + password)));
    return new Ajax(address, {
      'headers': {
        'Authorization': "Basic " + credentials,
        'Content-Type': "application/json"
      }
    });
  }

  playlists() {

    return this._getAjax().get(`/playlists`)
      .then(response => response.json().then(json => json))
      .then((json) => {
        return json.playlists;
      });
  }

  testConnection(host, password) {
    let logger = loggerCreator("testConnection", moduleLogger);
    logger.info(`start host: ${host}`);

    return this._getAjax(host, password).get(`/playlists`);
  }
}

export default new BackendMetadataApi();
