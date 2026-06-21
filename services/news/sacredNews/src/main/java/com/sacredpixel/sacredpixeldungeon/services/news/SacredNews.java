/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Sacred Pixel Dungeon
 * Copyright (C) 2026 AI SOFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.sacredpixel.sacredpixeldungeon.services.news;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.watabou.noosa.Game;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SacredNews extends NewsService {

	@Override
	public void checkForArticles(boolean useMetered, boolean preferHTTPS, NewsResultCallback callback) {

		if (!useMetered && !Game.platform.connectedToUnmeteredNetwork()){
			callback.onConnectionFailed();
			return;
		}

		Net.HttpRequest httpGet = new Net.HttpRequest(Net.HttpMethods.GET);
		// Always use HTTPS for security (HTTP fallback removed)
		httpGet.setUrl("https://shatteredpixel.com/feed_by_tag/SHPD_INGAME.xml");

		Gdx.net.sendHttpRequest(httpGet, new Net.HttpResponseListener() {
			@Override
			public void handleHttpResponse(Net.HttpResponse httpResponse) {
				ArrayList<NewsArticle> articles = new ArrayList<>();
				XmlReader reader = new XmlReader();
				XmlReader.Element xmlDoc = reader.parse(httpResponse.getResultAsStream());

				SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

				for (XmlReader.Element xmlArticle : xmlDoc.getChildrenByName("entry")){
					NewsArticle article = new NewsArticle();
					article.title = xmlArticle.get("title");
					try {
						article.date = dateParser.parse(xmlArticle.get("published"));
					} catch (ParseException e) {
						Game.reportException(e);
					}
					article.summary = xmlArticle.get("summary");
					article.URL = xmlArticle.getChildByName("link").getAttribute("href");
					// Ensure HTTPS is always used
					if (article.URL != null && article.URL.startsWith("http://")) {
						article.URL = article.URL.replace("http://", "https://");
					}

					Pattern versionCodeMatcher = Pattern.compile("v[0-9]+");
					try {
						Array<XmlReader.Element> properties = xmlArticle.getChildrenByName("category");
						for (XmlReader.Element prop : properties){
							String propVal = prop.getAttribute("term");
							if (propVal.startsWith("SHPD_ICON")){
								Matcher m = versionCodeMatcher.matcher(propVal);
								if (m.find()) {
									int iconGameVer = Integer.parseInt(m.group().substring(1));
									if (iconGameVer <= Game.versionCode) {
										article.icon = propVal.substring(propVal.indexOf(": ") + 2);
									}
								}
							}
						}
					} catch (Exception e){
						article.icon = null;
					}

					articles.add(article);
				}
				callback.onArticlesFound(articles);
			}

			@Override
			public void failed(Throwable t) {
				callback.onConnectionFailed();
			}

			@Override
			public void cancelled() {
				callback.onConnectionFailed();
			}
		});
	}

}
