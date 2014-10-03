var cloverplugin = {
    getMerchant: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'CloverPlugin', // mapped to our native Java class called "CalendarPlugin"
            'getMerchant', // with this action name
            []
        );
    }
}
module.exports=cloverplugin


