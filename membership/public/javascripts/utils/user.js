/*global escape:true */
define(['utils/atob'], function(AtoB){

    var self = this;

    var getUserOrSignIn = function(returnUrl){
        var user = getUserFromCookie();
        returnUrl = returnUrl || document.location.href;
        if (user) {
            return user;
        } else {
            window.location.href = '/signin?returnUrl=' + returnUrl;
        }
    };

    var getUserFromCookie = function(){
        var userFromCookieCache = null;

        function readCookie(name){
            var nameEQ = name + '=';
            var ca = document.cookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0) === ' ') { c = c.substring(1, c.length); }
                if (c.indexOf(nameEQ) === 0) { return c.substring(nameEQ.length, c.length); }
            }
            return null;
        }

        function decodeBase64(str){
            return decodeURIComponent(escape(new AtoB()(str.replace(/-/g, '+').replace(/_/g, '/').replace(/,/g, '='))));
        }

        if (userFromCookieCache === null) {
            var cookieData = readCookie('GU_U'),
                userData = cookieData ? JSON.parse(decodeBase64(cookieData.split('.')[0])) : null;
            if (userData) {
                userFromCookieCache = {
                    id: userData[0],
                    primaryemailaddress: userData[1], // this and displayname are non camelcase to match with formstack
                    displayname: userData[2],
                    accountCreatedDate: userData[6],
                    emailVerified: userData[7],
                    rawResponse: cookieData
                };
                self.userFromCookieCache = userFromCookieCache;
            }
        }

        return userFromCookieCache;
    };

    return {
        getUserOrSignIn: getUserOrSignIn,
        getUserFromCookie: getUserFromCookie
    };
});