package com.cybex.provider.graphene.eva;

public class EvaProject {
    private String logo;

    private String platform;

    private String ico_token_supply;

    private String token_price_in_usd;

    private String premium;

    private String token_name;

    private String score;

    private String risk_score;

    private String country;

    private String hype_score;

    private String id;

    private String description;

    private String name;

    private String industry;

    private String investment_rating;

    public String getLogo ()
    {
        return logo;
    }

    public void setLogo (String logo)
    {
        this.logo = logo;
    }

    public String getPlatform ()
    {
        return platform;
    }

    public void setPlatform (String platform)
    {
        this.platform = platform;
    }

    public String getIco_token_supply ()
    {
        return ico_token_supply;
    }

    public void setIco_token_supply (String ico_token_supply)
    {
        this.ico_token_supply = ico_token_supply;
    }

    public String getToken_price_in_usd ()
    {
        return token_price_in_usd;
    }

    public void setToken_price_in_usd (String token_price_in_usd)
    {
        this.token_price_in_usd = token_price_in_usd;
    }

    public String getPremium ()
    {
        return premium;
    }

    public void setPremium (String premium)
    {
        this.premium = premium;
    }

    public String getToken_name ()
    {
        return token_name;
    }

    public void setToken_name (String token_name)
    {
        this.token_name = token_name;
    }

    public String getScore ()
    {
        return score;
    }

    public void setScore (String score)
    {
        this.score = score;
    }

    public String getRisk_score ()
    {
        return risk_score;
    }

    public void setRisk_score (String risk_score)
    {
        this.risk_score = risk_score;
    }

    public String getCountry ()
    {
        return country;
    }

    public void setCountry (String country)
    {
        this.country = country;
    }

    public String getHype_score ()
    {
        return hype_score;
    }

    public void setHype_score (String hype_score)
    {
        this.hype_score = hype_score;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public String getDescription ()
    {
        return description;
    }

    public void setDescription (String description)
    {
        this.description = description;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getIndustry ()
    {
        return industry;
    }

    public void setIndustry (String industry)
    {
        this.industry = industry;
    }

    public String getInvestment_rating ()
    {
        return investment_rating;
    }

    public void setInvestment_rating (String investment_rating)
    {
        this.investment_rating = investment_rating;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [logo = "+logo+", platform = "+platform+", ico_token_supply = "+ico_token_supply+", token_price_in_usd = "+token_price_in_usd+", premium = "+premium+", token_name = "+token_name+", score = "+score+", risk_score = "+risk_score+", country = "+country+", hype_score = "+hype_score+", id = "+id+", description = "+description+", name = "+name+", industry = "+industry+", investment_rating = "+investment_rating+"]";
    }
}
