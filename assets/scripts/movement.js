function main() {
	
	function fly(p) {
		p.airTime = 0;
		p.canJump = true;
	}
	
    return {
        beforeProcessMove: function (player) {
			// fly(player);
        },
        afterProcessMove: function (player) {
        }
    }
}