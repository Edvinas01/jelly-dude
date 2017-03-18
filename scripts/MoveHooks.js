function main() {
    return {
        beforeProcessMove: function (player) {
			player.canJump = true;
			player.airTime = 0;
			
            Printer.printDetails(player);
        },
        afterProcessMove: function (player) {
        }
    }
}