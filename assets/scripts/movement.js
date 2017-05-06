function main() {
	
	function fly(p) {
		p.airTime = 0;
		p.canJump = true;
	}
	
    return {
        beforeMove: function (player) {
			// fly(player);
        },
        afterMove: function (player) {
        }
    }
}