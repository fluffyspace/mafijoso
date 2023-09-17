package eu.kodba.mafijoso

class DeathsAndCures {
    companion object{
        val cures = listOf(
            Speech("", "Srećom, doktor je reagirao na vrijeme", "Srećom, doktor je reagirao na vrijeme"),
            Speech("", "No udar stuje nije problem za ovog svemogućeg doktora!", "No udar stuje nije problem za ovog svemogućeg doktora!"),
            Speech("", "Ni jedan doktor nije ravan našem doktoru koji je odmah znao što činiti.", "Ni jedan doktor nije ravan našem doktoru koji je odmah znao što činiti."),
            Speech("", "Doktor je isti kafe aparat koristio za oživljavanje.", "Doktor je isti kafe aparat koristio za oživljavanje."),
            Speech("", "Doktor je izvršio reanimaciju udaranjem i puhanjem u stražnjicu.", "Doktor je izvršio reanimaciju udaranjem i puhanjem u stražnjicu."),
            Speech("", "Nakon prolaženja 7 planina i 7 mora, ubijanjem zmaja i nalaženjem sveznajućeg patuljka, doktor primjenjuje magiju kako bi oživio X.", "Nakon prolaženja 7 planina i 7 mora, ubijanjem zmaja i nalaženjem sveznajućeg patuljka, doktor primjenjuje magiju kako bi oživio X."),
            Speech("", "Doktor je odmah našao protuotrov i spasio X.", "Doktor je odmah našao protuotrov i spasio X."),
            Speech("", "Hvala Bogu, doktor je znao kako pomoći.", "Hvala Bogu, doktor je znao kako pomoći."),
            Speech("", "Doktoru ovo nije prvi slučaj predoziranja te je znao što činiti.", "Doktoru ovo nije prvi slučaj predoziranja te je znao što činiti."),
            Speech("", "Doktor odstranjuje lijepilo stavljanjem acetona u usta.", "Doktor odstranjuje lijepilo stavljanjem acetona u usta."),
            Speech("", "Srećom, doktor je njegov polubrat.", "Srećom, doktor je njezin polubrat."),
            Speech("", "Kupac je bio doktor te mu je vratio jedan bubreg.", "Kupac je bio doktor te joj je vratio jedan bubreg."),
            Speech("", "Saznalo se da je X alergičan na ubod pčela, srećom doktor je imao EpiPen.", "Saznalo se da je X alergična na ubod pčela, srećom doktor je imao EpiPen."),
            Speech("", "Doktor je natjerao mačke da se ispovrate te je selotejpom spojio X na mjesto.", "Doktor je natjerao mačke da se ispovrate te je selotejpom spojio X na mjesto."),
            Speech("", "Doktor je pogledao previše kriminalističkih serija te je stečeno znanje iskoristio kako bi pronašao X, zatočenog u podrumu psihopata.", "Doktor je pogledao previše kriminalističkih serija te je stečeno znanje iskoristio kako bi pronašao X, zatočenu u podrumu psihopata."),
            Speech("", "Doktor je lijevkom vratio krv u šupak i spasio X.", "Doktor je lijevkom vratio krv u šupak i spasio X."),
            Speech("", "Posjedujući crni pojas u karateu, doktor je izmlatao medvjeda, otvorio mu utrobu i spasio X.", "Posjedujući crni pojas u karateu, doktor je izmlatao medvjeda, otvorio mu utrobu i spasio X."),
            Speech("", "Doktor ga je uhvatio i dao mu malo mlijeka za smirenje.", "Doktor ju je uhvatio i dao joj malo mlijeka za smirenje."),
            Speech("", "Doktor je od mrtvog zobija napravio Frankensteina.", "Doktor je od mrtvog zobija napravio Frankensteina."),
            Speech("", "Doktor je podmornicu nazad napuhao sa kisikom, te je podmornica sigurno isplutala na površinu.", "Doktor je napuhao nazad podmornicu sa kisikom, te je podmornica sigurno isplutala na površinu."),
            Speech("", "Doktor je u svom slobodnom vremenu vatrogasac.", "Doktor je u svom slobodnom vremenu vatrogasac."),
        )

        val deaths = listOf(
            Speech(
                "",
                "Probudio vas je glasan vrisak. Sa drugog kata na glavu je pao X.",
                "Probudio vas je glasan vrisak. Sa drugog kata na glavu je pala X."
            ),
            Speech(
                "",
                "Vrijeme za jutarnju kaficu, no čim ste ušli u kuhinju, vidjeli ste X sa vilicom u tosteru.",
                "Vrijeme za jutarnju kaficu, no čim ste ušli u kuhinju, vidjeli ste X sa vilicom u tosteru.",
            ),
            Speech(
                "",
                "Zvoni telefon, bolnica vas obavještava da je X poginuo u prometnoj nesreći.",
                "Zvoni telefon, bolnica vas obavještava da je X poginula u prometnoj nesreći.",
            ),
            Speech(
                "",
                "Lokva krvi okružuje X, koji je bio prebijen do smrti sa kafe aparatom.",
                "Lokva krvi okružuje X, koja je bila prebijena do smrti sa kafe aparatom.",
            ),
            Speech(
                "",
                "U ladici nedostaje nož! Kasnije ga nalazite zabijenog u dupetu mrtvog X.",
                "U ladici nedostaje nož! Kasnije ga nalazite zabijenog u dupetu mrtvoe X.",
            ),
            Speech(
                "",
                "Nema ničega boljeg od tosta za doručak. Umjesto u kuhinji, toster nalazite kako se kupa u kadi sa X.",
                "Nema ničega boljeg od tosta za doručak. Umjesto u kuhinji, toster nalazite kako se kupa u kadi sa X.",
            ),
            Speech(
                "",
                "Dobro jutro kafopije! Pijete zajedno kaficu na terasi i uživate u pogledu, ali pogled vam skreće X koji se počinje gušiti te dramatično pada na pod.",
                "Dobro jutro kafopije! Pijete zajedno kaficu na terasi i uživate u pogledu, ali pogled vam skreće X koja se počinje gušiti te dramatično pada na pod.",
            ),
            Speech(
                "",
                "Prije uzimanja lijekova bitno je pogledati rok trajanja, X je to danas naučio.",
                "Prije uzimanja lijekova bitno je pogledati rok trajanja, X je to danas naučila.",
            ),
            Speech(
                "",
                "Što više to bolje! To si je mislio X dok je uzimao malo previše antidepresiva.",
                "Što više to bolje! To si je mislila X dok je uzimala malo previše antidepresiva.",
            ),
            Speech(
                "",
                "X je slučajno koristio lijepilo umjesto paste za zube.",
                "X je slučajno koristila lijepilo umjesto paste za zube.",
            ),
            Speech(
                "",
                "X je zaglavio u veš mašini i ugušio se.",
                "X je zaglavila u veš mašini i ugušila se.",
            ),
            Speech(
                "",
                "Kako bi zaradio za novi auto, X je prodao oba svoja bubrega.",
                "Kako bi zaradila za novi auto, X je prodala oba svoja bubrega.",
            ),
            Speech(
                "",
                "X se igrao sa psom, ubrzo je shvatio da, umjesto loptice, u ruci drži košnicu.",
                "X se igrao sa psom, ubrzo je shvatila da, umjesto loptice, u ruci drži košnicu.",
            ),
            Speech(
                "",
                "X je zaboravio nahraniti mačke koje nisu imale ništa za jest osim njega.",
                "X je zaboravila nahraniti mačke koje nisu imale ništa za jest osim nje.",
            ),
            Speech(
                "",
                "Očajan X je otišao na spoj sa prvom osobom koju je vidio na tinderu... Nikad se nije vratio.",
                "Očajana X je otišla na spoj sa prvom osobom koju je vidjela na tinderu... Nikad se nije vratila.",
            ),
            Speech(
                "",
                "Nekada, dildo može biti prevelik. X si je rastvorio šupak i raskrvario do smrti.",
                "Nekada, dildo može biti prevelik. X si je rastvorila šupak i raskrvarila do smrti.",
            ),
            Speech(
                "",
                "„Zašto bi medvjedi bili toliko slatki ako ih ne možeš dragati“ – zadnje riječi od X.",
                "„Zašto bi medvjedi bili toliko slatki ako ih ne možeš dragati“ – zadnje riječi od X.",
            ),
            Speech(
                "",
                "X je imao gadan LSD trip i mislio da može letjeti... Bacio se sa litice.",
                "X je imala gadan LSD trip i mislila da može letjeti... Bacila se sa litice.",
            ),
            Speech(
                "",
                "Nekada je teško razlikovati jestive od otrovnih gljiva. X je pojeo krivu vrstu gljiva, no, umjesto smrti, postao je zombi. Zatim ga je susjed upucao u glavu.",
                "Nekada je teško razlikovati jestive od otrovnih gljiva. X je pojela krivu vrstu gljiva, no, umjesto smrti, postala je zombi. Zatim ju je susjed upucao u glavu.",
            ),
            Speech(
                "",
                "Svaka čast, X je dobio na lotu! Sve novce odlučio je iskoristiti za put do titanika u podmornici kojom upravlja bilioner sa joystikom. Na kraju podmornica je implodirala... ",
                "Svaka čast, X je dobila na lotu! Sve novce odlučila je iskoristiti za put do titanika u podmornici kojom upravlja bilioner sa joystikom. Na kraju podmornica je implodirala... ",
            ),
            Speech(
                "",
                "X je zaspao sa cigarom u ustima i zapalio kuću.",
                "X je zaspala sa cigarom u ustima i zapalila kuću.",
            )
        )
    }
}