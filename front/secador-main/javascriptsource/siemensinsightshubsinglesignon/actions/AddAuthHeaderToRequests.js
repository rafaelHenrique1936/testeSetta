// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
import { Big } from "big.js";

// BEGIN EXTRA CODE
export function setWindowAccessToken(accessToken){	
	window.accessToken = accessToken;
}
function getOverwriteStatus() {
	if (window.overwrites === undefined){
		window.overwrites = {
			windowFetch : false,
			xmlhttpRequest : false
		}
	}
	return window.overwrites	
}
const Overwrites = {
	
}
// END EXTRA CODE

/**
 * @param {string} accessToken
 * @returns {Promise.<void>}
 */
export async function AddAuthHeaderToRequests(accessToken) {
	// BEGIN USER CODE
	if (!accessToken) {
		console.warn("AddAuthHeaderToRequests(): please provide an accessToken!");
		return;
	}
	
	setWindowAccessToken(accessToken);

	if (window.fetch && getOverwriteStatus().windowFetch === false) {		
		getOverwriteStatus().windowFetch = true; // Ensure Overwrite is done just once.
		var originalFetch = window.fetch;
		window.fetch = function(url, init) {
			if (!init) {
				init = {};
			}
			if (!init.headers) {
				init.headers = new Headers();
			}
			var tokenAvailable =
				typeof init.headers.get === 'function'
					? init.headers.get('Authorization')
					: init.headers.hasOwnProperty('Authorization');

			if (window.accessToken && !tokenAvailable) {
				if (typeof init.headers.set === 'function') {
					init.headers.set('Authorization', window.accessToken);
				} else {
					init.headers['Authorization'] = window.accessToken;
				}
			}
			return originalFetch(url, init);
		};
	} 

	if (window.XMLHttpRequest && getOverwriteStatus().xmlhttpRequest === false) {
		getOverwriteStatus().xmlhttpRequest = true; // Ensure Overwrite is done just once.
		var originalXMLHttpRequest = window.XMLHttpRequest;
		window.XMLHttpRequest = function() {
			var result = new originalXMLHttpRequest(arguments);

			// overwrite setRequestHeader function to make sure to set the Authorization header
			result.setRequestHeader = function(header, value) {
				if (header) {
					if (header.toLowerCase().indexOf('Authorization') !== -1) {
						if (this.authorizationSet === true) {
							// token is already in place -> so do nothing
							return;
						}
						this.authorizationSet = true;
					}
				}
				originalXMLHttpRequest.prototype.setRequestHeader.apply(this, arguments);
			};

			// overwrite open function to make sure to set the Authorization header at least once
			result.open = function() {
				originalXMLHttpRequest.prototype.open.apply(this, arguments);

				if (window.accessToken) {
					this.setRequestHeader('Authorization', window.accessToken);
				}
			};
			return result;
		};
	}
	// END USER CODE
}
