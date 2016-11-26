/**
 * Things to add:
 * 		- Field for entering user name at new game screen.
 * 		- JLabel at bottom of screen for dialog out to player
 * 		- Split hand input (not functional at moment)
 * 		- Test reseting the game after game over.
 * 		- 
 * 
 * @author Kailab Bowler
 * @author Kristofer Hoadley
 * @author Connor Premuda
 * @author Edward Woelke
 *
 */
public class GameLogic {
	private static boolean running;
	private static int FPS = 60;
	private static long targetTime = 1000/FPS;
	private static BlackJackGUI game;
	private static BlackjackLogic thisGame;
	private static Deck deck = Deck.getInstance();
	private static int betIncrement;
	
	public static int countDown = 1000;

	public static void main(String[] args){
		thisGame = new BlackjackLogic();
		game = new BlackJackGUI();
		game.setSize(400,600);
		running = true;
		betIncrement = 200;
		thisGame.addPlayer(new Player());
		thisGame.addPlayer(new Player());
		game.setPlayerName(thisGame.getPlayer().getName());
		game.setChips(Integer.toString(thisGame.getPlayer().getChips()));
		game.setBetAmount(Integer.toString(thisGame.getPlayer().getBet()));
		game.setShuffleAmount(Integer.toString(5));
		run();
		
	}
	public static void run(){
		long start;
		long elapsed;
		long wait;
		while(running){
			start = System.nanoTime();
			elapsed = System.nanoTime() - start;
			wait = (targetTime  - elapsed / 1000000 < 0 ? 5 : targetTime - elapsed / 1000000)*60;	//every second
			try{Thread.sleep(wait);}
			catch(Exception e){
				e.printStackTrace();}
			
			if(game.getPlayAgain()== false){
				game.EnablePlayAgain();
			}
			if (game.getDeal() == false && game.getPlayAgain()) {
				//when player wants to increase bet
				//doing betIncrement at 100 for now, we can make custom in a later version
				game.DisableNewGame();
				game.setFirstHand(true);
				game.DisableHit();
				game.DisableStay();
				game.DisableSplit();
				game.DisableDouble();
				game.EnableDeal();
				
				// enable or disable buttons for increasing and decreasing bet.
				if(thisGame.getPlayer().getChips()-thisGame.getPlayer().getBet()>=betIncrement)
					game.EnableIncrease();
				else
					game.DisableIncrease();
				if(thisGame.getPlayer().getBet()-betIncrement>0)
					game.EnableDecrease();
				else
					game.DisableDecrease();
				
				if(game.getIncrease()){
					thisGame.getPlayer().increaseBet(betIncrement);
					game.setBetAmount(Integer.toString(thisGame.getPlayer().getBet()));
					game.resetIncrease();
				}
				
				//when player wants to decrease bet
				if(game.getDecrease()){
					thisGame.getPlayer().decreaseBet(betIncrement);
					game.setBetAmount(Integer.toString(thisGame.getPlayer().getBet()));
					game.resetDecrease();
				}
			}
			else {
				// Runs only once right after "deal" is pressed.
				if(game.getFirstHand()){
					game.DisableDeal();
					game.DisableDecrease();
					game.DisableIncrease();
					game.EnableHit();
					game.EnableStay();
					game.EnableSplit();
					game.EnableDouble();
					game.resetHit();
					game.resetDouble();
					game.resetDecrease();
					game.resetIncrease();
					game.resetStay();
					if(thisGame.getRounds()%5==0){
						deck.shuffle();
					}
					game.setShuffleAmount(Integer.toString(5-thisGame.getRounds()%5));
					
					
					thisGame.removeDeadPlayers();
					thisGame.discardHands();
					if(!thisGame.someoneHasChips()){
						game.EnablePlayAgain();
						game.DisableDeal();
						game.DisableHit();
						game.DisableStay();
						game.DisableSplit();
						game.DisableDecrease();
						game.DisableIncrease();
						game.DisableDouble();
						thisGame = new BlackjackLogic();
						thisGame.addPlayer(new Player());
						thisGame.addPlayer(new Player());
						game.setPlayerName(thisGame.getPlayer().getName());
						game.setChips(Integer.toString(thisGame.getPlayer().getChips()));
						game.setBetAmount(Integer.toString(thisGame.getPlayer().getBet()));
					}
					else{
						game.DisableNewGame();
					}
					
					thisGame.dealHand(deck);
					thisGame.dealHand(deck);
					game.setPlayerCards(thisGame.getPlayer().getHand());
					game.setDealerCards(thisGame.getDealer().getHand());
					thisGame.getPlayer().removeChips(thisGame.getPlayer().getBet());
					game.setChips(Integer.toString(thisGame.getPlayer().getChips()));
					game.setFirstHand(false);
					if(thisGame.getPlayer().hasBlackjack())
						thisGame.getPlayer().setStay();
				}
				
				//logic for allowing player to split
				if(thisGame.getPlayer().canSplit()){
					game.EnableSplit();	
				}
				else{
					game.DisableSplit();
				}
				if(game.getSplit()){
					thisGame.getPlayer().splitHand();
					game.setPlayerCards(thisGame.getPlayer().getHand());
					game.setChips(Integer.toString(thisGame.getPlayer().getChips()));
				}
				
				//logic for allowing player to double
				if(thisGame.getPlayer().canDouble()){
					game.EnableDouble();	
				}
				else{
					game.DisableDouble();
				}
				//when player has doubled
				if(game.getDouble()){
					thisGame.getPlayer().removeChips(thisGame.getPlayer().getBet());
					game.setChips(Integer.toString(thisGame.getPlayer().getChips()));
					game.DisableDouble();
					thisGame.getPlayer().receiveCard(deck.dealCard());
					game.setPlayerCards(thisGame.getPlayer().getHand());
					thisGame.getPlayer().setStay();
				}
				
				//When player has hit
				if(game.getHit()){
					thisGame.getPlayer().receiveCard(deck.dealCard());
					game.setPlayerCards(thisGame.getPlayer().getHand());
					game.DisableDouble();
					if(thisGame.getPlayer().getHandValue()>21){
						thisGame.getPlayer().setStay();
					}
					game.resetHit();
				}
				//when player has stayed
				//disable all buttons, set dealer's cards
				if(game.getStay()){
					thisGame.getPlayer().setStay();
				}
				if(thisGame.getPlayer().getHasStayed()){
					game.resetStay();
					game.DisablePlayer();
					game.DisableDouble();
					thisGame.getPlayer().resetStay();	
					BlackjackLogic.endTurn();			
					}

				//// NPC logic
				if(thisGame.getTurn().isNPC()){
					thisGame.npcTurn();
					if(thisGame.getTurn()==thisGame.getDealer()){
						game.setDealerCards(thisGame.getDealer().getHand());
						game.EnableDeal();
						game.resetDeal();
						game.getDeal();
						thisGame.payTheWinners();
						game.setChips(Integer.toString(thisGame.getPlayer().getChips()));
						if(!thisGame.someoneHasChips())
							game.resetNewGame();
					}
					BlackjackLogic.endTurn();
				}
			}	
		}
	}
}
