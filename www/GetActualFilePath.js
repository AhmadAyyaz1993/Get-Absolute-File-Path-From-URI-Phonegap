var GetActualFilePath = {

    GetActualFilePath : function(uri, successCallback, failureCallback) {
        cordova.exec(successCallback, failureCallback, 'GetActualFilePathPlugin',
            'getActualFilePath', [{"Uri":uri}]);
    }
};

module.exports = GetActualFilePath;