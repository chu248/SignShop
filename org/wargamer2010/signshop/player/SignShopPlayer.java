package org.wargamer2010.signshop.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.listeners.SignShopPlayerListener;

import net.milkbowl.vault.economy.EconomyResponse;

public class SignShopPlayer {
    Player ssPlayer = null;
    String sPlayername = "";
    
    public SignShopPlayer(String sName) {
        Player[] players = Bukkit.getServer().getOnlinePlayers();

        for(Player pPlayer : players){
            if(pPlayer.getName().equals(sName)){
                ssPlayer = pPlayer;                                
            }
        }
        sPlayername = sName;
    }
    
    public SignShopPlayer(Player pPlayer) {
        ssPlayer = pPlayer;
        if(ssPlayer != null)
            sPlayername = ssPlayer.getName();
    }

    public static void broadcastMsg(World world, String sMessage) {
        Player[] players = Bukkit.getServer().getOnlinePlayers();
        for(Player player : players)
            if(player.getWorld() == world)
                player.sendMessage(ChatColor.GOLD+"[SignShop] [" + world.getName() + "] " + ChatColor.WHITE + sMessage);
    }

    public void sendMessage(String sMessage) {
        if(sMessage == null || sMessage.trim().equals("") || ssPlayer == null)
            return;
        ssPlayer.sendMessage(ChatColor.GOLD + "[SignShop] " + ChatColor.WHITE + sMessage);
    }
    
    public String getName() {
        return sPlayername;
    }
    
    public Player getPlayer() {
        return ssPlayer;
    }
    
    public Boolean hasPerm(String perm, Boolean OPOperation) { 
        if(Vault.permission == null || ssPlayer == null) {
            return true;
        }
        Boolean isOP = ssPlayer.isOp();
        Boolean OPOverride = SignShop.getOPOverride();
        if(SignShop.USE_PERMISSIONS && isOP && !OPOverride)
            ssPlayer.setOp(false);
        
        if(SignShop.USE_PERMISSIONS && OPOverride && isOP)
            return true;
        else if(SignShop.USE_PERMISSIONS && Vault.permission.playerHas((String)null, sPlayername, perm)) {
            ssPlayer.setOp(isOP);
            return true;
        } else if(!SignShop.USE_PERMISSIONS && isOP)
            return true;
        else if(!SignShop.USE_PERMISSIONS && !OPOperation)
            return true;
        ssPlayer.setOp(isOP);
        return false;           
    }
    
    public Boolean hasMoney(float amount) {        
        if(Vault.economy == null || sPlayername.equals(""))
            return false;
        else
            return Vault.economy.has(sPlayername, amount);
    }
    
    public Boolean mutateMoney(float amount) {
        if(Vault.economy == null || sPlayername.equals(""))
            return false;
        EconomyResponse response;
        if(amount > 0.0)
            response = Vault.economy.depositPlayer(sPlayername, amount);
        else if(amount < 0.0)
            response = Vault.economy.withdrawPlayer(sPlayername, Math.abs(amount));
        else
            return true;
        if(response.type == EconomyResponse.ResponseType.SUCCESS)
            return true;
        else
            return false;
    }
    
    public void givePlayerItems(ItemStack[] isItemsToTake) {
        if(ssPlayer == null)
            return;
        ItemStack[] isBackup = new ItemStack[isItemsToTake.length];        
        for(int i = 0; i < isItemsToTake.length; i++){
            if(isItemsToTake[i] != null){
                isBackup[i] = new ItemStack(
                    isItemsToTake[i].getType(),
                    isItemsToTake[i].getAmount(),
                    isItemsToTake[i].getDurability()
                );
                SignShopPlayerListener.addSafeEnchantments(isBackup[i], isItemsToTake[i].getEnchantments());                
                if(isItemsToTake[i].getData() != null){
                    isBackup[i].setData(isItemsToTake[i].getData());
                }
            }
        }
        ssPlayer.getInventory().addItem(isBackup);
    }
    
    public Float getPlayerPricemod(String sOperation, Boolean bBuyorSell) {
        Float fPricemod = 1.0f;
        Float fTemp = fPricemod;
        if(Vault.permission == null || ssPlayer == null)
            return fPricemod;
        String[] sGroups = Vault.permission.getPlayerGroups(ssPlayer);
        if(sGroups.length == 0)
            return fPricemod;
        for(int i = 0; i < sGroups.length; i++) {
            String sGroup = sGroups[i].toLowerCase();
            if(SignShop.PriceMultipliers.containsKey(sGroup) && SignShop.PriceMultipliers.get(sGroup).containsKey(sOperation)) {
                fTemp = SignShop.PriceMultipliers.get(sGroup).get(sOperation);
                if(bBuyorSell && fTemp < fPricemod)
                    fPricemod = fTemp;
                else if(!bBuyorSell && fTemp > fPricemod)
                    fPricemod = fTemp;
            }
        }       
        return fPricemod;
    }
    
}
