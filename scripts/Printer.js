var Printer = (function () {
    return {
        printDetails: function (player) {
            if (player) {
                print('FPS: ' + game.getFps() + ', HP: ' + player.health);
            } else {
                print('FPS: ' + game.getFps());
            }
        }
    }
})();