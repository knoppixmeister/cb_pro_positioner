package lv.bizapps.position;

import lv.bizapps.positioner.utils.*;

public class Calc {
	public static double calcPriceIncrSum(double buyPrice, final double buySum, final double cleanPfSum, final int maxIncs, double commBuyPercent, double commSellPercent) {
		buyPrice = Math.ceil(buyPrice);

		final double	startCommSum	=	Utils.round(buySum/100*commBuyPercent, 2),
						outCommSum		=	Utils.round(buySum/100*commSellPercent, 2),
						totalCommSum	=	Utils.round(startCommSum+outCommSum, 2);

		double sellPrice = buyPrice+1;
		double cleanProfitSum;

		while(true) {
			cleanProfitSum = Utils.round(Utils.round(buySum/100*Utils.round((sellPrice*100d/buyPrice)-100, 2), 2)-totalCommSum, 2);

			if(cleanProfitSum >= cleanPfSum) return Math.ceil(sellPrice-buyPrice);

			sellPrice += 1;

			if(sellPrice-buyPrice > maxIncs) return -1;
		}
	}
}
