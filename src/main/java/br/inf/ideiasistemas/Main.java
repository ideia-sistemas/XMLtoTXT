package br.inf.ideiasistemas;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class Main {

    private static Map<String, String> list = new HashMap<>();
    private static String itemDet = "";
    private static String id = "";
    private static String versao = "";
    private static int dup = 0;
    private static int det = 0;
    private static int vol = 0;
    private static int rastro = 0;
    private static int chavesRef = 0;

    /**
     * Realiza a leitura e parser do xml, deixando os dados disponiveis em um
     * hashmap. As chaves do hash estÃ£o definidas de acordo com o nome da tag
     * unido com a tag pai segindo o formato: TagPai.TagFilho No caso de itens
     * tags que se repetem, como os itens da nota, deve se respeitar o padÃ£o:
     * tagPai + numero contador de itens + .tagfilho (ex. Item1.atributo)
     */
    private Map<String, String> lerarq(String arq) {

        File f = new File(arq);
        SAXBuilder sb = new SAXBuilder();

        Document d;
        try {
            d = sb.build(f);
//recupera o elemento root
            Element nfe = d.getRootElement();

//Recupera atributos filhos (Attributes)
            List atributes = nfe.getAttributes();
            Iterator i_atr = atributes.iterator();

//Itera atributos filhos
            while (i_atr.hasNext()) {
                Attribute atrib = (Attribute) i_atr.next();
            }

//Recupera elementos filhos (children)
            List elements = nfe.getChildren();
            Iterator i = elements.iterator();

//Itera elementos filhos, e filhos do dos filhos
            while (i.hasNext()) {
                Element element = (Element) i.next();
                list = trataElement(element);
            }

        } catch (JDOMException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        return list;

    }

    private Map<String, String> trataElement(Element element) {

//Recuper atributos filhos (Attributes)
        List atributes = element.getAttributes();
        Iterator i_atr = atributes.iterator();

//Itera atributos filhos
        while (i_atr.hasNext()) {
            Attribute atrib = (Attribute) i_atr.next();
            String x = (element.getName());
            if (element.getName().equals("det")) {
                rastro = 0;
                if (atrib.getValue() != null) {
                    itemDet = atrib.getValue();
                } else {
                    itemDet = "";
                }

            } else {
                itemDet = "";
            }
            if (element.getName().equals("infNFe")) {
                if (atrib.getName().equals("Id")) {
                    id = atrib.getValue();
                }
                if (atrib.getName().equals("versao")) {
                    versao = atrib.getValue();
                }
            }

        }
//Recupera  elementos filhos (children)
        List elements = element.getChildren();
        Iterator it = elements.iterator();

        //Itera elementos filhos, e filhos do dos filhos
        while (it.hasNext()) {
            String tags = "ide|emit|dest|total|total|infAdic|Signature|infNFe";

            Element el = (Element) it.next();
            if (el.getName().equals("vol")) {
                vol++;
                el.setName(el.getName() + vol);
            }
            if ("cobr.dup".equals(element.getName() + "." + el.getName())) {
                dup++;
            } else if ("infNFe.det".equals(element.getName() + "." + el.getName())) {
                det++;
            }
            if ((element.getName() + "." + el.getName()).contains("rastro")) {
                if ((element.getName() + "." + el.getName()).equals("prod.rastro")) {
                    rastro++;
                }
                list.put("prod" + itemDet + "." + element.getName() + rastro + "." + el.getName(), new String(el.getText()));
            } else if ((element.getName() + "." + el.getName()).contains("NFref")) {
                if ((element.getName() + "." + el.getName()).equals("NFref.refNFe")) {
                    chavesRef++;
                    list.put((element.getName() + "." + chavesRef), new String(el.getText()));
                }
            } else if ((element.getName() + "." + el.getName()).contains(".dup") || (element.getName() + "." + el.getName()).contains("dup.")) {
                list.put(element.getName() + dup + "." + el.getName(), new String(el.getText()));
            } else {
                if (tags.contains(element.getName()) && !element.getName().contains("det")) {
                    itemDet = "";
                }
                list.put(element.getName() + itemDet + "." + el.getName(), new String(el.getText()));
            }
//            if ((element.getName() + "." + el.getName()).contains("rastro")) {
//                if ((element.getName() + "." + el.getName()).equals("prod.rastro")) {
//                    rastro++;
//                }
//                list.put("prod" + itemDet + "." + element.getName() + rastro + "." + el.getName(), new String(el.getText()));
//            } else if ((element.getName() + "." + el.getName()).contains(".dup") || (element.getName() + "." + el.getName()).contains("dup.")) {
//                list.put(element.getName() + dup + "." + el.getName(), new String(el.getText()));
//            } else {
//                if (tags.contains(element.getName()) && !element.getName().contains("det")) {
//                    itemDet = "";
//                }
//                list.put(element.getName() + itemDet + "." + el.getName(), new String(el.getText()));
//            }

            //            System.out.println("--------------------------------" + el.getName());
            trataElement(el);
        }
        vol = 0;
        return list;
    }

    /**
     * Realiza o preenchimento do template da nfe 3.10 versÃ£o txt, obdecendo as
     * condiÃ§Ãµes para cada item.
     */
    public static StringBuffer geraDoc310(HashMap<String, String> x) {
        int volume = 1;
        StringBuffer o = new StringBuffer();
        o.append("NOTA FISCAL|1|\n");
        o.append("0|" + id + "|\n");
        o.append("A|" + versao + "|NFe|\n");
        o.append("B|" + x.get("ide.cUF") + "|" + x.get("ide.cNF") + "|" + x.get("ide.natOp") + "|" + x.get("ide.indPag") + "|" + x.get("ide.mod") + "|" + x.get("ide.serie") + "|" + x.get("ide.nNF") + "|" + x.get("ide.dhEmi") + "|" + x.get("ide.dhSaiEnt") + "|" + x.get("ide.tpNF") + "|" + x.get("ide.idDest") + "|" + x.get("ide.cMunFG") + "|" + x.get("ide.tpImp") + "|" + x.get("ide.tpEmis") + "|" + x.get("ide.cDV") + "|" + x.get("ide.tpAmb") + "|" + x.get("ide.finNFe") + "|" + x.get("ide.indFinal") + "|" + x.get("ide.indPres") + "|" + x.get("ide.procEmi") + "|" + x.get("ide.verProc") + "|" + x.get("ide.dhCont") + "|" + x.get("ide.xJust") + "|\n");
//Complementos de B
        if (x.get("NFref.refNFe") != null) {
            o.append("B13|" + x.get("NFref.refNFe") + "|\n");
        } else if (x.get("refNF.AAMM") != null && x.get("refNF.CNPJ") != null) {
            o.append("B14|" + x.get("refNF.cUF") + "|" + x.get("refNF.AAMM") + "|" + x.get("refNF.CNPJ") + "|" + x.get("refNF.mod") + "|" + x.get("refNF.serie") + "|" + x.get("refNF.nNF") + "|\n");
        } else if (x.get("refNFP.AAMM") != null && x.get("refNFP.IE") != null) {
            o.append("B20a|" + x.get("refNFP.cUF") + "|" + x.get("refNFP.AAMM") + "|" + x.get("refNFP.IE") + "|" + x.get("refNFP.mod") + "|" + x.get("refNFP.serie") + "|" + x.get("refNFP.nNF") + "|\n");
            if (x.get("refNFP.CNPJ") != null) {
                o.append("B20d|" + x.get("refNFP.CNPJ") + "|\n");
            } else if (x.get("refNFP.CPF") != null) {
                o.append("B20d|" + x.get("refNFP.CPF") + "|\n");
            }
        } else if (x.get("NFref.refCTe") != null) {
            o.append("B20i|" + x.get("NFref.refCTe") + "|\n");
        } else if (x.get("refECF.nECF") != null) {
            o.append("B20j|" + x.get("refECF.mod") + "|" + x.get("refECF.nECF") + "|" + x.get("refECF.nCOO") + "|");
        }
//        ----------------------------------------------------
        o.append("C|").append(x.get("emit.xNome")).append("|").append(x.get("emit.xFant")).append("|").append(x.get("emit.IE")).append("|").append(x.get("emit.IEST")).append("|").append(x.get("emit.IM")).append("|").append(x.get("emit.CNAE")).append("|").append(x.get("emit.CRT")).append("|\n");
        //Complementos de C
        if (x.get("emit.CNPJ") != null) {
            o.append("C02|" + x.get("emit.CNPJ") + "|\n");
        } else if (x.get("emit.CPF") != null) {
            o.append("C02a|" + x.get("emit.CPF") + "|\n");
        }
        if (x.get("emit.enderEmit") != null) {
            o.append("C05|" + x.get("enderEmit.xLgr") + "|" + x.get("enderEmit.nro") + "|" + x.get("enderEmit.xCpl") + "|" + x.get("enderEmit.xBairro") + "|" + x.get("enderEmit.cMun") + "|" + x.get("enderEmit.xMun") + "|" + x.get("enderEmit.UF") + "|" + x.get("enderEmit.CEP") + "|" + x.get("enderEmit.cPais") + "|" + x.get("enderEmit.xPais") + "|" + x.get("enderEmit.fone") + "|\n");
        }
//        ----------------------------------------------------
        //  LINHA D IGNORADA
        // D|CNPJ|xOrgao|matr|xAgente|fone|UF|nDAR|dEmi|vDAR|repEmi|dPag|
//        ----------------------------------------------------
        o.append("E|" + x.get("dest.xNome") + "|" + x.get("dest.indIEDest") + "|" + x.get("dest.IE") + "|" + x.get("dest.ISUF") + "|" + x.get("dest.IM") + "|" + x.get("dest.email") + "|\n");
//        VERIFICAR SOBRA DE UM CAMPO ANTES DO EMAIL NO ITEM E
        //Complementos de E
        if (x.get("dest.CNPJ") != null) {
            o.append("E02|" + x.get("dest.CNPJ") + "|\n");
        } else if (x.get("dest.CPF") != null) {
            o.append("E02a|" + x.get("dest.CPF") + "|\n");
        }
        if (x.get("dest.enderDest") != null) {
            o.append("E05|" + x.get("enderDest.xLgr") + "|" + x.get("enderDest.nro") + "|" + x.get("enderDest.xCpl") + "|" + x.get("enderDest.xBairro") + "|" + x.get("enderDest.cMun") + "|" + x.get("enderDest.xMun") + "|" + x.get("enderDest.UF") + "|" + x.get("enderDest.CEP") + "|" + x.get("enderDest.cPais") + "|" + x.get("enderDest.xPais") + "|" + x.get("enderDest.fone") + "|\n");
        }
//        ----------------------------------------------------
//        ITENS IGNORADOS
//        if (x.get("enderEmit.xLgr") + "|" + x.get("enderEmit.nro") + "|" + x.get("enderEmit.xCpl") + "|" + x.get("enderEmit.xBairro") + "|" + x.get("enderEmit.cMun") == ) {
//
//        }
//        o.append("F|" + x.get("enderEmit.xLgr") + "|" + x.get("enderEmit.nro") + "|" + x.get("enderEmit.xCpl") + "|" + x.get("enderEmit.xBairro") + "|" + x.get("enderEmit.cMun") + "|" + x.get("enderEmit.xMun") + "|" + x.get("enderEmit.UF") + "|\n");
//        Comprementos de F
//        if (x.get("emit.CNPJ") != null) {
//            o.append("F02|" + x.get("emit.CNPJ") + "|\n");
//        } else if (x.get("emit.CPF") != null) {
//            o.append("F02a|" + x.get("emit.CPF") + "|\n");
//        }
//        ----------------------------------------------------
//        o.append("G|" + x.get("enderDest.xLgr") + "|" + x.get("enderDest.nro") + "|" + x.get("enderDest.xCpl") + "|" + x.get("enderDest.xBairro") + "|" + x.get("enderDest.cMun") + "|" + x.get("enderDest.xMun") + "|" + x.get("enderDest.UF") + "|\n");
////        Comprementos de F
//        if (x.get("dest.CNPJ") != null) {
//            o.append("G02|" + x.get("dest.CNPJ") + "|\n");
//        } else if (x.get("dest.CPF") != null) {
//            o.append("G02a|" + x.get("dest.CPF") + "|\n");
//        }
//        ----------------------------------------------------
//ITENS DA NOTA
        int item = 1;
        while (x.get("det" + item + ".prod") != null) {

            o.append("H|" + item + "|" + x.get("det" + item + ".infAdProd") + "| \n");
//        ----------------------------------------------------
//            System.out.println("----------------------..." + x.get("prod" + item + ".vSeg"));
            o.append("I|" + x.get("prod" + item + ".cProd") + "|" + x.get("prod" + item + ".cEAN") + "|" + x.get("prod" + item + ".xProd") + "|" + x.get("prod" + item + ".NCM") + "|" + x.get("prod" + item + ".EXTIPI") + "|" + x.get("prod" + item + ".CFOP") + "|" + x.get("prod" + item + ".uCom") + "|" + x.get("prod" + item + ".qCom") + "|" + x.get("prod" + item + ".vUnCom") + "|" + x.get("prod" + item + ".vProd") + "|" + x.get("prod" + item + ".CEANTrib") + "|" + x.get("prod" + item + ".uTrib") + "|" + x.get("prod" + item + ".qTrib") + "|" + x.get("prod" + item + ".vUnTrib") + "|" + x.get("prod" + item + ".vFrete") + "|" + x.get("prod" + item + ".vSeg") + "|" + x.get("prod" + item + ".vDesc") + "|" + x.get("prod" + item + ".vOutro") + "|" + x.get("prod" + item + ".indTot") + "|" + x.get("prod" + item + ".xPed") + "|" + x.get("prod" + item + ".nItemPed") + "|" + x.get("prod" + item + ".nFCI") + "|" + x.get("prod" + item + ".nRECOPI") + "|\n");
            if (x.get("prod" + item + ".DI") != null) {
                o.append("I18|" + x.get("DI" + item + ".nDI") + "|" + x.get("DI" + item + ".dDI") + "|" + x.get("DI" + item + ".xLocDesemb") + "|" + x.get("DI" + item + ".UFDesemb") + "|" + x.get("DI" + item + ".dDesemb") + "|" + x.get("DI" + item + ".cExportador") + "|" + x.get("DI" + item + ".tpViaTransp") + "|" + x.get("DI" + item + ".vAFRMM") + "|" + x.get("DI" + item + ".tpIntermedio") + "|" + x.get("DI" + item + ".CNPJ") + "|" + x.get("DI" + item + ".UFTerceiro") + "|\n");
                if (x.get("DI" + item + ".ADI") != null) {
                    o.append("I25|" + x.get("ADI" + item + ".nAdicao") + "|" + x.get("ADI" + item + ".nSeqAdic|cFabricante") + "|" + x.get("ADI" + item + ".vDescDI") + "|" + x.get("ADI" + item + ".nDraw") + "|\n");
                }
            }
            if (x.get("prod" + item + ".detExport") != null) {
                o.append("I50|" + x.get("ADI" + item + ".detExport") + "|\n");
                if (x.get("detExport" + item + ".exportInd") != null) {
                    o.append("I52|" + x.get("exportInd" + item + ".nRE") + "|" + x.get("exportInd" + item + ".chNFe") + "|" + x.get("exportInd" + item + ".qExport") + "|\n");
                }
            }

            if (x.get("prod" + item + ".NVE") != null) {
                o.append("I99|" + x.get("prod" + item + ".NVE") + "|\n");
            }
//        ----------------------------------------------------
            //Veiculos
            if (x.get("prod" + item + ".veicProd") != null) {
                o.append("J|" + x.get("veicProd" + item + ".TpOp") + "|" + x.get("veicProd" + item + ".chassi") + "|" + x.get("veicProd" + item + ".cCor") + "|" + x.get("veicProd" + item + ".xCor") + "|" + x.get("veicProd" + item + ".pot") + "|" + x.get("veicProd" + item + ".cilin") + "|" + x.get("veicProd" + item + ".pesoL") + "|" + x.get("veicProd" + item + ".pesoB") + "|" + x.get("veicProd" + item + ".nSerie") + "|" + x.get("veicProd" + item + ".tpComb") + "|" + x.get("veicProd" + item + ".nMotor") + "|" + x.get("veicProd" + item + ".CMT") + "|" + x.get("veicProd" + item + ".dist") + "|" + x.get("veicProd" + item + ".anoMod") + "|" + x.get("veicProd" + item + ".anoFab") + "|" + x.get("veicProd" + item + ".tpPint") + "|" + x.get("veicProd" + item + ".tpVeic") + "|" + x.get("veicProd" + item + ".espVeic") + "|" + x.get("veicProd" + item + ".VIN") + "|" + x.get("veicProd" + item + ".condVeic") + "|" + x.get("veicProd" + item + ".cMod") + "|" + x.get("veicProd" + item + ".cCorDENATRAN") + "|" + x.get("veicProd" + item + ".lota") + "|" + x.get("veicProd" + item + ".tpRest") + "|\n");
            } //        ----------------------------------------------------
            //Medicamentos
            else if (x.get("prod" + item + ".med") != null) {
                o.append("K|" + x.get("med" + item + ".nLote") + "|" + x.get("med" + item + ".qLote") + "|" + x.get("med" + item + ".dFab") + "|" + x.get("med" + item + ".dVal") + "|" + x.get("med" + item + ".vPMC") + "|\n");
            } //        ----------------------------------------------------
            //armamento
            else if (x.get("prod" + item + ".arma") != null) {
                o.append("L|" + x.get("arma" + item + ".tpArma") + "|" + x.get("arma" + item + ".nSerie") + "|" + x.get("arma" + item + ".nCano") + "|" + x.get("arma" + item + ".descr") + "|\n");
            } //        ----------------------------------------------------
            // combustÃ­vel
            else if (x.get("prod" + item + ".comb") != null) {
                o.append("L01|" + x.get("comb" + item + ".cProdANP") + "|" + x.get("comb" + item + ".CODIF") + "|" + x.get("comb" + item + ".qTemp") + "|" + x.get("comb" + item + ".UFCons") + "|" + x.get("comb" + item + ".pMixGN") + "|\n");
                if (x.get("comb" + item + ".CIDE") != null) {
                    o.append("L105|" + x.get("CIDE" + item + ".qBCProd") + "|" + x.get("CIDE" + item + ".vAliqProd") + "|" + x.get("CIDE" + item + ".vCIDE") + "|\n");

                }
            }
//        ----------------------------------------------------
            o.append("M|" + x.get("imposto" + item + ".vTotTrib") + "|\n");
//        ----------------------------------------------------
            o.append("N|\n");

            if (x.get("ICMS" + item + ".ICMS00") != null) {
                o.append("N02|" + x.get("ICMS00" + item + ".orig") + "|" + x.get("ICMS00" + item + ".CST") + "|" + x.get("ICMS00" + item + ".modBC") + "|" + x.get("ICMS00" + item + ".vBC") + "|" + x.get("ICMS00" + item + ".pICMS") + "|" + x.get("ICMS00" + item + ".vICMS") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS10") != null) {
                o.append("N03|" + x.get("ICMS10" + item + ".orig") + "|" + x.get("ICMS10" + item + ".CST") + "|" + x.get("ICMS10" + item + ".modBC") + "|" + x.get("ICMS10" + item + ".vBC") + "|" + x.get("ICMS10" + item + ".pICMS") + "|" + x.get("ICMS10" + item + ".vICMS") + "|" + x.get("ICMS10" + item + ".modBCST") + "|" + x.get("ICMS10" + item + ".pMVAST") + "|" + x.get("ICMS10" + item + ".pRedBCST") + "|" + x.get("ICMS10" + item + ".vBCST") + "|" + x.get("ICMS10" + item + ".pICMSST") + "|" + x.get("ICMS10" + item + ".vICMSST") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS20") != null) {
                o.append("N04|" + x.get("ICMS20" + item + ".orig") + "|" + x.get("ICMS20" + item + ".CST") + "|" + x.get("ICMS20" + item + ".modBC") + "|" + x.get("ICMS20" + item + ".pRedBC") + "|" + x.get("ICMS20" + item + ".vBC") + "|" + x.get("ICMS20" + item + ".pICMS") + "|" + x.get("ICMS20" + item + ".vICMS") + "|" + x.get("ICMS20" + item + ".vICMSDeson") + "|" + x.get("ICMS20" + item + ".motDesICMS") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS30") != null) {
                o.append("N05|" + x.get("ICMS30" + item + ".orig") + "|" + x.get("ICMS30" + item + ".CST") + "|" + x.get("ICMS30" + item + ".modBCST") + "|" + x.get("ICMS30" + item + ".PMVAST") + "|" + x.get("ICMS30" + item + ".pRedBCST") + "|" + x.get("ICMS30" + item + ".vBCST") + "|" + x.get("ICMS30" + item + ".pICMSST") + "|" + x.get("ICMS30" + item + ".vICMSST") + "|" + x.get("ICMS30" + item + ".vICMSDeson") + "|" + x.get("ICMS30" + item + ".motDesICMS") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS40") != null || x.get("ICMS" + item + ".ICMS41") != null || x.get("ICMS" + item + ".ICMS50") != null) {
                String tagPai = "";
                if (x.get("ICMS" + item + ".ICMS40") != null) {
                    tagPai = "ICMS40" + item;
                } else if (x.get("ICMS" + item + ".ICMS41") != null) {
                    tagPai = "ICMS41" + item;
                } else if (x.get("ICMS" + item + ".ICMS50") != null) {
                    tagPai = "ICMS50" + item;
                }
                o.append("N06|" + x.get(tagPai + ".orig") + "|" + x.get(tagPai + ".CST") + "|" + x.get(tagPai + ".vICMSDeson") + "|" + x.get(tagPai + ".motDesICMS") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS51") != null) {
                o.append("N07|" + x.get("ICMS51" + item + ".orig") + "|" + x.get("ICMS51" + item + ".CST") + "|" + x.get("ICMS51" + item + ".modBC") + "|" + x.get("ICMS51" + item + ".pRedBC") + "|" + x.get("ICMS51" + item + ".VBC") + "|" + x.get("ICMS51" + item + ".pICMS") + "|" + x.get("ICMS51" + item + ".vICMSOp") + "|" + x.get("ICMS51" + item + ".pDif") + "|" + x.get("ICMS51" + item + ".vICMSDif") + "|" + x.get("ICMS51" + item + ".vICMS") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS60") != null) {
                o.append("N08|" + x.get("ICMS60" + item + ".orig") + "|" + x.get("ICMS60" + item + ".CST") + "|" + x.get("ICMS60" + item + ".vBCSTRet") + "|" + x.get("ICMS60" + item + ".vICMSSTRet") + "|\n");

            } else if (x.get("ICMS" + item + ".ICMS70") != null) {
                o.append("N09|" + x.get("ICMS70" + item + ".orig") + "|" + x.get("ICMS70" + item + ".CST") + "|" + x.get("ICMS70" + item + ".modBC") + "|" + x.get("ICMS70" + item + ".pRedBC") + "|" + x.get("ICMS70" + item + ".vBC") + "|" + x.get("ICMS70" + item + ".pICMS") + "|" + x.get("ICMS70" + item + ".vICMS") + "|" + x.get("ICMS70" + item + ".modBCST") + "|" + x.get("ICMS70" + item + ".pMVAST") + "|" + x.get("ICMS70" + item + ".pRedBCST") + "|" + x.get("ICMS70" + item + ".vBCST") + "|" + x.get("ICMS70" + item + ".pICMSST") + "|" + x.get("ICMS70" + item + ".vICMSST") + "|" + x.get("ICMS70" + item + ".vICMSDeson") + "|" + x.get("ICMS70" + item + ".motDesICMS") + "|\n");

            } else if (x.get("ICMS" + item + ".ICMS90") != null) {
                o.append("N10|" + x.get("ICMS90" + item + ".orig") + "|" + x.get("ICMS90" + item + ".CST") + "|" + x.get("ICMS90" + item + ".modBC") + "|" + x.get("ICMS90" + item + ".vBC") + "|" + x.get("ICMS90" + item + ".pRedBC") + "|" + x.get("ICMS90" + item + ".pICMS") + "|" + x.get("ICMS90" + item + ".vICMS") + "|" + x.get("ICMS90" + item + ".modBCST") + "|" + x.get("ICMS90" + item + ".pMVAST") + "|" + x.get("ICMS90" + item + ".pRedBCST") + "|" + x.get("ICMS90" + item + ".vBCST") + "|" + x.get("ICMS90" + item + ".pICMSST") + "|" + x.get("ICMS90" + item + ".vICMSST") + "|" + x.get("ICMS90" + item + ".vICMSDeson") + "|" + x.get("ICMS90" + item + ".motDesICMS") + "|\n");
            }
            //ITENS IGNORADOS
            //N10a|orig|CST|modBC|pRedBC|vBC|pICMS|vICMS|modBCST|pMVAST|pRedBCST|vBCST|pICMSST|vICMSST|pBCOp|UFST|
            //N10b|orig|CST|vBCSTRet|vICMSSTRet|vBCSTDest|vICMSSTDest|
            //N10c|orig|CSOSN|pCredSN|vCredICMSSN|
            //N10d|orig|CSOSN|
            //N10e|orig|CSOSN|modBCST|pMVAST|pRedBCST|vBCST|pICMSST|vICMSST|pCredSN|vCredICMSSN|
            //N10f|orig|CSOSN|modBCST|pMVAST|pRedBCST|vBCST|pICMSST|vICMSST|
            //N10g|orig|CSOSN|vBCSTRet|vICMSSTRet|
            //N10h|orig|CSOSN|modBC|vBC|pRedBC|pICMS|vICMS|modBCST|pMVAST|pRedBCST|vBCST|pICMSST|vICMSST|pCredSN|vCredICMSSN|
            //        ----------------------------------------------------
            if (x.get("imposto" + item + ".IPI") != null) {
                o.append("O|" + x.get("IPI" + item + ".CNPJProd") + "|" + x.get("IPI" + item + ".cSelo") + "|" + x.get("IPI" + item + ".qSelo") + "|" + x.get("IPI" + item + ".cEnq") + "|\n");

                if ((x.get("IPI" + item + ".IPITrib") != null)) {
                    o.append("O07|" + x.get("IPITrib" + item + ".CST") + "|" + x.get("IPITrib" + item + ".vIPI") + "|\n");
                    if (x.get("IPITrib" + item + ".pIPI") != null) {
                        o.append("O10|" + x.get("IPITrib" + item + ".vBC") + "|" + x.get("IPITrib" + item + ".pIPI") + "|\n");
                    } else {
                        o.append("O11|" + x.get("IPITrib" + item + ".qUnid") + "|" + x.get("IPITrib" + item + ".vUnid") + "|\n");
                    }
                } else if ((x.get("IPI" + item + ".IPINT") != null)) {
                    o.append("O08|" + x.get("IPINT" + item + ".CST") + "|\n");
                }
            }

//        ----------------------------------------------------
//            IMPOSTO DE IMPORTAÃ‡Ã‚O
            if (x.get("imposto" + item + ".II") != null) {
//                System.out.println("II" + item + ".VBC" + x.get("II" + item + ".VBC"));
                o.append("P|" + x.get("II" + item + ".vBC") + "|" + x.get("II" + item + ".vDespAdu") + "|" + x.get("II" + item + ".vII") + "|" + x.get("II" + item + ".vIOF") + "|\n");
            }
//        ----------------------------------------------------
            if (x.get("imposto" + item + ".PIS") != null) {
                o.append("Q|\n");
                if (x.get("PISAliq" + item + ".CST") != null) {
                    o.append("Q02|" + x.get("PISAliq" + item + ".CST") + "|" + x.get("PISAliq" + item + ".vBC") + "|" + x.get("PISAliq" + item + ".pPIS") + "|" + x.get("PISAliq" + item + ".vPIS") + "|\n");
                } else if (x.get("PISQtde" + item + ".CST") != null) {
                    o.append("Q03|" + x.get("PISQtde" + item + ".CST") + "|" + x.get("PISQtde" + item + ".qBCProd") + "|" + x.get("PISQtde" + item + ".vAliqProd") + "|\n");
                } else if (x.get("PISNT" + item + ".CST") != null) {
                    o.append("Q04|" + x.get("PISNT" + item + ".CST") + "|\n");
                } else if (x.get("PISOutr" + item + ".CST") != null) {
                    o.append("Q05|" + x.get("PISOutr" + item + ".CST") + "|" + x.get("PISOutr" + item + ".vPIS") + "|\n");
                }
            }
//        ----------------------------------------------------
            if (x.get("PIS" + item + ".PISST") != null) {
                o.append("R|" + x.get("PISST" + item + ".vPIS") + "|\n");

                if (x.get("PISST" + item + ".pPIS") != null) {
                    o.append("R02|" + x.get("PISST" + item + ".vBC") + "|" + x.get("PISST" + item + ".pPIS") + "|\n");
                } else {
                    o.append("R04|" + x.get("PISST" + item + ".qBCProd") + "|" + x.get("PISST" + item + ".vAliqProd") + "|\n");
                }
            }
//        ----------------------------------------------------
            if (x.get("imposto" + item + ".COFINS") != null) {
                o.append("S|\n");
                if (x.get("COFINSAliq" + item + ".CST") != null) {
                    o.append("S02|" + x.get("COFINSAliq" + item + ".CST") + "|" + x.get("COFINSAliq" + item + ".vBC") + "|" + x.get("COFINSAliq" + item + ".pCOFINS") + "|" + x.get("COFINSAliq" + item + ".vCOFINS") + "|\n");
                } else if (x.get("COFINSQtde" + item + ".CST") != null) {
                    o.append("S03|" + x.get("COFINSQtde" + item + ".CST") + "|" + x.get("COFINSQtde" + item + ".qBCProd") + "|" + x.get("COFINSQtde" + item + ".vAliqProd") + "|" + x.get("COFINSQtde" + item + ".vCOFINS") + "|\n");
                } else if (x.get("COFINSNT" + item + ".CST") != null) {
                    o.append("S04|" + x.get("COFINSNT" + item + ".CST") + "|\n");
                } else if (x.get("COFINSOutr" + item + ".CST") != null) {
                    o.append("S05|" + x.get("COFINSOutr" + item + ".CST") + "|" + x.get("COFINSOutr" + item + ".vCOFINS") + "|\n");
                    if (x.get("COFINSOutr" + item + ".pCOFINS") != null) {
                        o.append("S07|" + x.get("COFINSOutr" + item + ".vBC") + "|" + x.get("COFINSOutr" + item + ".pCOFINS") + "|\n");
                    } else {
                        o.append("S06|" + x.get("COFINSOutr" + item + ".qBCProd") + "|" + x.get("COFINSOutr" + item + ".vAliqProd") + "|\n");
                    }
                }
//        ----------------------------------------------------
                if (x.get("COFINS" + item + ".COFINSST") != null) {
                    o.append("T|" + x.get("COFINSST" + item + ".vCOFINS") + "|\n");
                    if (x.get("COFINSST" + item + ".pCOFINS") != null) {
                        o.append("T02|" + x.get("COFINSST" + item + ".vBC") + "|" + x.get("COFINSST" + item + ".pCOFINS") + "|\n");
                    } else {
                        o.append("T04|" + x.get("COFINSST" + item + ".qBCProd") + "|" + x.get("COFINSST" + item + ".vAliqProd") + "|\n");
                    }
                }
            }
//        ----------------------------------------------------
            //            ITEM IGNORADO
            //U|vBC|vAliq|vISSQN|cMunFG|cListServ|vDeducao|vOutro|vDescIncond|vDescCond|vISSRet|indISS|cServico|cM
            //un|cPais|nProcesso|indIncentivo|

//        ----------------------------------------------------
//            System.out.println("++++++++++++++++++++" + item);
            item++;

        }
//        FIM DE ITENS
//        ----------------------------------------------------
        o.append("W|\n");
        if (x.get("total.ICMSTot") != null) {
            o.append("W02|" + x.get("ICMSTot.vBC") + "|" + x.get("ICMSTot.vICMS") + "|" + x.get("ICMSTot.vICMSDeson") + "|" + x.get("ICMSTot.vBCST") + "|" + x.get("ICMSTot.vST") + "|" + x.get("ICMSTot.vProd") + "|" + x.get("ICMSTot.vFrete") + "|" + x.get("ICMSTot.vSeg") + "|" + x.get("ICMSTot.vDesc") + "|" + x.get("ICMSTot.vII") + "|" + x.get("ICMSTot.vIPI") + "|" + x.get("ICMSTot.vPIS") + "|" + x.get("ICMSTot.vCOFINS") + "|" + x.get("ICMSTot.vOutro") + "|" + x.get("ICMSTot.vNF") + "|" + x.get("ICMSTot.vTotTrib") + "|\n");
        }
        if (x.get("total.ISSQNTot") != null) {
            o.append("W17|" + x.get("ISSQNTot.vServ") + "|" + x.get("ISSQNTot.vBC") + "|" + x.get("ISSQNTot.vISS") + "|" + x.get("ISSQNTot.vPIS") + "|" + x.get("ISSQNTot.vCOFINS") + "|" + x.get("ISSQNTot.dCompet") + "|" + x.get("ISSQNTot.vDeducao") + "|" + x.get("ISSQNTot.vOutro") + "|" + x.get("ISSQNTot.vDescIncond") + "|" + x.get("ISSQNTot.vDescCond") + "|" + x.get("ISSQNTot.vISSRet") + "|" + x.get("ISSQNTot.cRegTrib") + "|\n");
        }
        if (x.get("total.retTrib ") != null) {
            o.append("W23|" + x.get("retTrib.vRetPIS") + "|" + x.get("retTrib.vRetCOFINS") + "|" + x.get("retTrib.vRetCSLL") + "|" + x.get("retTrib.vBCIRRF") + "|" + x.get("retTrib.vIRRF") + "|" + x.get("retTrib.vBCRetPrev") + "|" + x.get("retTrib.vRetPrev") + "|\n");
        }
//        ----------------------------------------------------
        o.append("X|" + x.get("transp.modFrete") + "|\n");
        if ((x.get("transporta.xNome") != null) || (x.get("transporta.IE") != null) || (x.get("transporta.xEnder") != null) || (x.get("transporta.UF") != null) || (x.get("transporta.xMun") != null)) {
            o.append("X03|" + x.get("transporta.xNome") + "|" + x.get("transporta.IE") + "|" + x.get("transporta.xEnder") + "|" + x.get("transporta.UF") + "|" + x.get("transporta.xMun") + "|\n");
        }
        if (x.get("transporta.CNPJ") != null) {
            o.append("X04|" + x.get("transporta.CNPJ") + "|\n");
        }
        if (x.get("transporta.CPF") != null) {
            o.append("X05|" + x.get("transporta.CPF") + "|\n");
        }
        if (x.get("transp.retTransp") != null) {
            o.append("X11|" + x.get("veicTransp.vServ") + "|" + x.get("veicTransp.vBCRet") + "|" + x.get("veicTransp.pICMSRet") + "|" + x.get("veicTransp.vICMSRet") + "|" + x.get("veicTransp.CFOP") + "|" + x.get("veicTransp.cMunFG") + "|\n");
        }
        if (x.get("transp.veicTransp") != null) {
            o.append("X18|" + x.get("veicTransp.Placa") + "|" + x.get("veicTransp.UF") + "|" + x.get("veicTransp.RNTC") + "|\n");
        }
        //        IGNORADo
        //        [0a 5] {
        //X22 | Placa | UF | RNTC |
        //        }
        //    }
        //        [0 a 5000] {
//        X26|qVol|esp|marca|nVol|pesoL|pesoB|

        //[0 a 5000] {
        if (x.get("transp.vol" + volume) != null) {
            o.append("X26|" + x.get("vol" + volume + ".qVol") + "|" + x.get("vol" + volume + ".esp") + "|" + x.get("vol" + volume + ".marca") + "|" + x.get("vol" + volume + ".nVol") + "|" + x.get("vol" + volume + ".pesoL") + "|" + x.get("vol" + volume + ".pesoB") + "|\n");

            volume++;
        }
        //X33|nLacre|
        //}
        //}
//        ----------------------------------------------------
        o.append("Y|\n");
        if (x.get("cobr.fat") != null) {
            o.append("Y02|" + x.get("fat.nFat") + "|" + x.get("fat.vOrig") + "|" + x.get("fat.vDesc") + "|" + x.get("fat.vLiq") + "|\n");
        }
        for (int i = 1; i <= 120; i++) {
            if (x.get("cobr" + i + ".dup") != null) {
                o.append("Y07|" + x.get("dup" + i + ".nDup") + "|" + x.get("dup" + i + ".dVenc") + "|" + x.get("dup" + i + ".vDup") + "|\n");
            } else {
                break;
            }
        }
        if (x.get("pag.detPag.tPag") != null) {
            o.append("YA|" + x.get("pag.detPag.indPag") + "|" + x.get("pag.detPag.tPag") + "|" + x.get("pag.detPag.vPag") + "|");
        }
        if (x.get("pag.detPag.card") != null) {
            o.append("YA04|" + x.get("pag.detPag.card.tpIntegra") + "|" + x.get("pag.detPag.card.CNPJ") + "|" + x.get("pag.detPag.card.tBand") + "|" + x.get("pag.detPag.card.cAut") + "|");
        }

//        [0 ou 1] {
//Z|InfAdFisco|InfCpl|
        if ((x.get("infAdic.InfAdFisco") != null) || (x.get("infAdic.infCpl") != null)) {
            o.append("Z|" + x.get("infAdic.infAdFisco") + "|" + x.get("infAdic.infCpl") + "|\n");
        }

//[0 a 10] {
//Z04|xCampo|xTexto|
        for (int i = 1; i <= 10; i++) {
            if (x.get("obsCont" + i + ".xCampo") != null || x.get("obsCont" + i + ".xTexto") != null) {
                o.append("Z04|" + x.get("obsCont" + i + ".xCampo") + "|" + x.get("obsCont" + i + ".xTexto") + "|\n");
            } else {
                break;
            }
        }
//}
//[0 a 10] {w
//Z07|xCampo|xTexto|
        for (int i = 1; i <= 10; i++) {
            if (x.get("obsFisco" + i + ".xCampo") != null || x.get("obsFisco" + i + ".xTexto") != null) {
                o.append("Z07|" + x.get("obsFisco" + i + ".xCampo") + "|" + x.get("obsFisco" + i + ".xTexto") + "|\n");
            } else {
                break;
            }
        }
//}
//[0 a 100] {
//Z10|nProc|indProc|
        for (int i = 1; i <= 10; i++) {
            if (x.get("procRef" + i + ".nProc") != null || x.get("procRef" + i + ".indProc") != null) {
                o.append("Z10|" + x.get("procRef" + i + ".nProc") + "|" + x.get("procRef" + i + ".indProc") + "|\n");
            } else {
                break;
            }
        }
//}
//}
//[0 ou 1] {
//ZA|UFSaidaPais|xLocExporta|xLocDespacho|
        if (x.get("exporta.UFSaidaPais") != null) {
            o.append("ZA|" + x.get("exporta.UFSaidaPais") + "|" + x.get("exporta.xLocExporta") + "|" + x.get("exporta.xLocDespacho") + "|\n");
        }
//}
//[0 ou 1] {
//ZB|xNEmp|xPed|xCont|
        if (x.get("compra.UFSaidaPais") != null) {
            o.append("ZB|" + x.get("compra.xNEmp") + "|" + x.get("compra.xPed") + "|" + x.get("compra.xCont") + "|\n");
        }
//}
//[0 ou 1] {
//ZC01|safra|ref|qTotMes|qTotAnt|qTotGer|vFor|vTotDed|vLiqFor|
//        if (x.get("compra.UFSaidaPais") != null) {
//            o.append("ZC01|" + x.get("compra.xNEmp") + "|" + x.get("compra.xPed") + "|" + x.get("compra.xCont") + "|\n");
//        }
//[1 a 31] {
//ZC04|dia|qtde|
//}
//[0 a 10] {
//ZC10|xDed|vDed|
//}
//}
        x = null;
        return o;
    }

    /**
     * Realiza o preenchimento do template da nfe 4.00 versÃ£o txt, obdecendo as
     * condiÃ§Ãµes para cada item.
     */
    public static StringBuffer geraDoc400(HashMap<String, String> x) {
        int volume = 1;
        StringBuffer o = new StringBuffer();
        o.append("NOTA FISCAL|1|\n");
        o.append("0|" + id + "|\n");
        o.append("A|" + versao + "|NFe|\n");
        o.append("B|" + x.get("ide.cUF") + "|" + x.get("ide.cNF") + "|" + x.get("ide.natOp") + "|" + x.get("ide.mod") + "|" + x.get("ide.serie") + "|" + x.get("ide.nNF") + "|" + x.get("ide.dhEmi") + "|" + x.get("ide.dhSaiEnt") + "|" + x.get("ide.tpNF") + "|" + x.get("ide.idDest") + "|" + x.get("ide.cMunFG") + "|" + x.get("ide.tpImp") + "|" + x.get("ide.tpEmis") + "|" + x.get("ide.cDV") + "|" + x.get("ide.tpAmb") + "|" + x.get("ide.finNFe") + "|" + x.get("ide.indFinal") + "|" + x.get("ide.indPres") + "|" + x.get("ide.procEmi") + "|" + x.get("ide.verProc") + "|" + x.get("ide.dhCont") + "|" + x.get("ide.xJust") + "|\n");

//Complementos de B
        if (x.get("NFref.1") != null) {
            int i = 1;
            while (x.get("NFref." + i) != null) {
                o.append("B13|" + x.get("NFref." + i) + "|\n");
                i++;
            }
        } else if (x.get("refNF.AAMM") != null && x.get("refNF.CNPJ") != null) {
            o.append("B14|" + x.get("refNF.cUF") + "|" + x.get("refNF.AAMM") + "|" + x.get("refNF.CNPJ") + "|" + x.get("refNF.mod") + "|" + x.get("refNF.serie") + "|" + x.get("refNF.nNF") + "|\n");
        } else if (x.get("refNFP.AAMM") != null && x.get("refNFP.IE") != null) {
            o.append("B20a|" + x.get("refNFP.cUF") + "|" + x.get("refNFP.AAMM") + "|" + x.get("refNFP.IE") + "|" + x.get("refNFP.mod") + "|" + x.get("refNFP.serie") + "|" + x.get("refNFP.nNF") + "|\n");
            if (x.get("refNFP.CNPJ") != null) {
                o.append("B20d|" + x.get("refNFP.CNPJ") + "|\n");
            } else if (x.get("refNFP.CPF") != null) {
                o.append("B20d|" + x.get("refNFP.CPF") + "|\n");
            }
        } else if (x.get("NFref.refCTe") != null) {
            o.append("B20i|" + x.get("NFref.refCTe") + "|\n");
        } else if (x.get("refECF.nECF") != null) {
            o.append("B20j|" + x.get("refECF.mod") + "|" + x.get("refECF.nECF") + "|" + x.get("refECF.nCOO") + "|");
        }
//        ----------------------------------------------------
        o.append("C|").

                append(x.get("emit.xNome")).

                append("|").

                append(x.get("emit.xFant")).

                append("|").

                append(x.get("emit.IE")).

                append("|").

                append(x.get("emit.IEST")).

                append("|").

                append(x.get("emit.IM")).

                append("|").

                append(x.get("emit.CNAE")).

                append("|").

                append(x.get("emit.CRT")).

                append("|\n");
        //Complementos de C
        if (x.get("emit.CNPJ") != null) {
            o.append("C02|" + x.get("emit.CNPJ") + "|\n");
        } else if (x.get("emit.CPF") != null) {
            o.append("C02a|" + x.get("emit.CPF") + "|\n");
        }
        if (x.get("emit.enderEmit") != null) {
            o.append("C05|" + x.get("enderEmit.xLgr") + "|" + x.get("enderEmit.nro") + "|" + x.get("enderEmit.xCpl") + "|" + x.get("enderEmit.xBairro") + "|" + x.get("enderEmit.cMun") + "|" + x.get("enderEmit.xMun") + "|" + x.get("enderEmit.UF") + "|" + x.get("enderEmit.CEP") + "|" + x.get("enderEmit.cPais") + "|" + x.get("enderEmit.xPais") + "|" + x.get("enderEmit.fone") + "|\n");
        }
//        ----------------------------------------------------
        //  LINHA D IGNORADA
        // D|CNPJ|xOrgao|matr|xAgente|fone|UF|nDAR|dEmi|vDAR|repEmi|dPag|
//        ----------------------------------------------------
        o.append("E|" + x.get("dest.xNome") + "|" + x.get("dest.indIEDest") + "|" + x.get("dest.IE") + "|" + x.get("dest.ISUF") + "|" + x.get("dest.IM") + "|" + x.get("dest.email") + "|\n");
//        VERIFICAR SOBRA DE UM CAMPO ANTES DO EMAIL NO ITEM E
        //Complementos de E
        if (x.get("dest.CNPJ") != null) {
            o.append("E02|" + x.get("dest.CNPJ") + "|\n");
        } else if (x.get("dest.CPF") != null) {
            o.append("E02a|" + x.get("dest.CPF") + "|\n");
        }
        if (x.get("dest.enderDest") != null) {
            o.append("E05|" + x.get("enderDest.xLgr") + "|" + x.get("enderDest.nro") + "|" + x.get("enderDest.xCpl") + "|" + x.get("enderDest.xBairro") + "|" + x.get("enderDest.cMun") + "|" + x.get("enderDest.xMun") + "|" + x.get("enderDest.UF") + "|" + x.get("enderDest.CEP") + "|" + x.get("enderDest.cPais") + "|" + x.get("enderDest.xPais") + "|" + x.get("enderDest.fone") + "|\n");
        }

        //        ----------------------------------------------------
//        ITENS IGNORADOS
//        if (x.get("enderEmit.xLgr") + "|" + x.get("enderEmit.nro") + "|" + x.get("enderEmit.xCpl") + "|" + x.get("enderEmit.xBairro") + "|" + x.get("enderEmit.cMun") == ) {
//
//        }
//        o.append("F|" + x.get("enderEmit.xLgr") + "|" + x.get("enderEmit.nro") + "|" + x.get("enderEmit.xCpl") + "|" + x.get("enderEmit.xBairro") + "|" + x.get("enderEmit.cMun") + "|" + x.get("enderEmit.xMun") + "|" + x.get("enderEmit.UF") + "|\n");
//        Comprementos de F
//        if (x.get("emit.CNPJ") != null) {
//            o.append("F02|" + x.get("emit.CNPJ") + "|\n");
//        } else if (x.get("emit.CPF") != null) {
//            o.append("F02a|" + x.get("emit.CPF") + "|\n");
//        }
//        ----------------------------------------------------
//        o.append("G|" + x.get("enderDest.xLgr") + "|" + x.get("enderDest.nro") + "|" + x.get("enderDest.xCpl") + "|" + x.get("enderDest.xBairro") + "|" + x.get("enderDest.cMun") + "|" + x.get("enderDest.xMun") + "|" + x.get("enderDest.UF") + "|\n");
////        Comprementos de F
//        if (x.get("dest.CNPJ") != null) {
//            o.append("G02|" + x.get("dest.CNPJ") + "|\n");
//        } else if (x.get("dest.CPF") != null) {
//            o.append("G02a|" + x.get("dest.CPF") + "|\n");
//        }
//        ----------------------------------------------------
//ITENS DA NOTA
        int item = 1;
        while (x.get("det" + item + ".prod") != null) {

            o.append("H|" + item + "|" + x.get("det" + item + ".infAdProd") + "| \n");
//        ----------------------------------------------------
//            System.out.println("----------------------..." + x.get("prod" + item + ".vSeg"));
            o.append("I|" + x.get("prod" + item + ".cProd") + "|" + x.get("prod" + item + ".cEAN") + "|" + x.get("prod" + item + ".xProd") + "|" + x.get("prod" + item + ".NCM") + "|" + x.get("prod" + item + ".cBenef") + "|" + x.get("prod" + item + ".EXTIPI") + "|" + x.get("prod" + item + ".CFOP") + "|" + x.get("prod" + item + ".uCom") + "|" + x.get("prod" + item + ".qCom") + "|" + x.get("prod" + item + ".vUnCom") + "|" + x.get("prod" + item + ".vProd") + "|" + x.get("prod" + item + ".CEANTrib") + "|" + x.get("prod" + item + ".uTrib") + "|" + x.get("prod" + item + ".qTrib") + "|" + x.get("prod" + item + ".vUnTrib") + "|" + x.get("prod" + item + ".vFrete") + "|" + x.get("prod" + item + ".vSeg") + "|" + x.get("prod" + item + ".vDesc") + "|" + x.get("prod" + item + ".vOutro") + "|" + x.get("prod" + item + ".indTot") + "|" + x.get("prod" + item + ".xPed") + "|" + x.get("prod" + item + ".nItemPed") + "|" + x.get("prod" + item + ".nFCI") + "|" + x.get("prod" + item + ".nRECOPI") + "|\n");
            if (x.get("prod" + item + ".DI") != null) {
                o.append("I18|" + x.get("DI" + item + ".nDI") + "|" + x.get("DI" + item + ".dDI") + "|" + x.get("DI" + item + ".xLocDesemb") + "|" + x.get("DI" + item + ".UFDesemb") + "|" + x.get("DI" + item + ".dDesemb") + "|" + x.get("DI" + item + ".tpViaTransp") + "|" + x.get("DI" + item + ".vAFRMM") + "|" + x.get("DI" + item + ".tpIntermedio") + "|" + x.get("DI" + item + ".cExportador") + "|" + x.get("DI" + item + ".tpViaTransp") + "|" + x.get("DI" + item + ".vAFRMM") + "|" + x.get("DI" + item + ".tpIntermedio") + "|" + x.get("DI" + item + ".CNPJ") + "|" + x.get("DI" + item + ".UFTerceiro") + "|\n");
                if (x.get("DI" + item + ".ADI") != null) {
                    o.append("I25|" + x.get("ADI" + item + ".nAdicao") + "|" + x.get("ADI" + item + ".nSeqAdic") + "|" + x.get("ADI" + item + ".cFabricante") + "|" + x.get("ADI" + item + ".vDescDI") + "|" + x.get("ADI" + item + ".nDraw") + "|\n");
                }
                if (x.get("DI" + item + ".adi") != null) {
                    o.append("I25|" + x.get("adi" + item + ".nAdicao") + "|" + x.get("adi" + item + ".nSeqAdic") + "|" + x.get("adi" + item + ".cFabricante") + "|" + x.get("adi" + item + ".vDescDI") + "|" + x.get("adi" + item + ".nDraw") + "|\n");
                }
            }
            if (x.get("prod" + item + ".CEST") != null) {
                o.append("I05C|" + x.get("prod" + item + ".CEST") + "|\n");

            }
            if (x.get("prod" + item + ".indEscala") != null) {
                o.append("I05D|" + x.get("prod" + item + ".indEscala") + "|\n");

            }
            if (x.get("prod" + item + ".CNPJFab") != null) {
                o.append("I05E|" + x.get("prod" + item + ".CNPJFab") + "|\n");

            }
            if (x.get("prod" + item + ".cBenef") != null) {
                o.append("I05F|" + x.get("prod" + item + ".cBenef") + "|\n");

            }
            if (x.get("prod" + item + ".detExport") != null) {
                o.append("I50|" + x.get("ADI" + item + ".detExport") + "|\n");
                if (x.get("detExport" + item + ".exportInd") != null) {
                    o.append("I52|" + x.get("exportInd" + item + ".nRE") + "|" + x.get("exportInd" + item + ".chNFe") + "|" + x.get("exportInd" + item + ".qExport") + "|\n");
                }
            }

            if (x.get("prod" + item + ".NVE") != null) {
                o.append("I99|" + x.get("prod" + item + ".NVE") + "|\n");
            }
//          --------------------------------------------------
//          Rastreabilidade
            if (x.get("prod" + item + ".rastro1.nLote") != null) {
                for (int i = 1; i <= 120; i++) {
                    if (x.get("prod" + item + ".rastro" + i + ".nLote") != null) {
                        o.append("I80|" + x.get("prod" + item + ".rastro" + i + ".nLote") + "|" + x.get("prod" + item + ".rastro" + i + ".qLote") + "|" + x.get("prod" + item + ".rastro" + i + ".dFab") + "|" + x.get("prod" + item + ".rastro" + i + ".dVal") + "|" + x.get("prod" + item + ".rastro" + i + ".cAgreg") + "|\n");
                    } else {
                        break;
                    }
                }
            }
//        ----------------------------------------------------
            //Veiculos
            if (x.get("prod" + item + ".veicProd") != null) {
                o.append("J|" + x.get("veicProd" + item + ".TpOp") + "|" + x.get("veicProd" + item + ".chassi") + "|" + x.get("veicProd" + item + ".cCor") + "|" + x.get("veicProd" + item + ".xCor") + "|" + x.get("veicProd" + item + ".pot") + "|" + x.get("veicProd" + item + ".cilin") + "|" + x.get("veicProd" + item + ".pesoL") + "|" + x.get("veicProd" + item + ".pesoB") + "|" + x.get("veicProd" + item + ".nSerie") + "|" + x.get("veicProd" + item + ".tpComb") + "|" + x.get("veicProd" + item + ".nMotor") + "|" + x.get("veicProd" + item + ".CMT") + "|" + x.get("veicProd" + item + ".dist") + "|" + x.get("veicProd" + item + ".anoMod") + "|" + x.get("veicProd" + item + ".anoFab") + "|" + x.get("veicProd" + item + ".tpPint") + "|" + x.get("veicProd" + item + ".tpVeic") + "|" + x.get("veicProd" + item + ".espVeic") + "|" + x.get("veicProd" + item + ".VIN") + "|" + x.get("veicProd" + item + ".condVeic") + "|" + x.get("veicProd" + item + ".cMod") + "|" + x.get("veicProd" + item + ".cCorDENATRAN") + "|" + x.get("veicProd" + item + ".lota") + "|" + x.get("veicProd" + item + ".tpRest") + "|\n");
            } //        ----------------------------------------------------
            //Medicamentos
            else if (x.get("prod" + item + ".med") != null) {
                o.append("K|" + x.get("med" + item + ".cProdANVISA") + "|" + x.get("med" + item + ".vPMC") + "|\n");
            } //        ----------------------------------------------------
            //armamento
            else if (x.get("prod" + item + ".arma") != null) {
                o.append("L|" + x.get("arma" + item + ".tpArma") + "|" + x.get("arma" + item + ".nSerie") + "|" + x.get("arma" + item + ".nCano") + "|" + x.get("arma" + item + ".descr") + "|\n");
            } //        ----------------------------------------------------
            // combustÃ­vel
//            else if (x.get("prod" + item + ".comb") != null) {
//                o.append("L01|" + x.get("comb" + item + ".cProdANP") + "|" + x.get("comb" + item + ".CODIF") + "|" + x.get("comb" + item + ".qTemp") + "|" + x.get("comb" + item + ".UFCons") + "|" + x.get("comb" + item + ".pMixGN") + "|\n");
//                if (x.get("comb" + item + ".CIDE") != null) {
//                    o.append("L105|" + x.get("CIDE" + item + ".qBCProd") + "|" + x.get("CIDE" + item + ".vAliqProd") + "|" + x.get("CIDE" + item + ".vCIDE") + "|\n");

//                }
//            }
//        ----------------------------------------------------
            o.append("M|" + x.get("imposto" + item + ".vTotTrib") + "|\n");
//        ----------------------------------------------------
            o.append("N|\n");

            if (x.get("ICMS" + item + ".ICMS00") != null) {
                o.append("N02|" + x.get("ICMS00" + item + ".orig") + "|" + x.get("ICMS00" + item + ".CST") + "|" + x.get("ICMS00" + item + ".modBC") + "|" + x.get("ICMS00" + item + ".vBC") + "|" + x.get("ICMS00" + item + ".pICMS") + "|" + x.get("ICMS00" + item + ".vICMS") + "|" + x.get("ICMS00" + item + ".pFCP") + "|" + x.get("ICMS00" + item + ".vFCP") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS10") != null) {
                o.append("N03|" + x.get("ICMS10" + item + ".orig") + "|" + x.get("ICMS10" + item + ".CST") + "|" + x.get("ICMS10" + item + ".modBC") + "|" + x.get("ICMS10" + item + ".vBC") + "|" + x.get("ICMS10" + item + ".pICMS") + "|" + x.get("ICMS10" + item + ".vICMS") + "|" + x.get("ICMS10" + item + ".vBCFCP") + "|" + x.get("ICMS10" + item + ".pFCP") + "|" + x.get("ICMS10" + item + ".vFCP") + "|" + x.get("ICMS10" + item + ".modBCST") + "|" + x.get("ICMS10" + item + ".pMVAST") + "|" + x.get("ICMS10" + item + ".pRedBCST") + "|" + x.get("ICMS10" + item + ".vBCST") + "|" + x.get("ICMS10" + item + ".pICMSST") + "|" + x.get("ICMS10" + item + ".vICMSST") + "|" + x.get("ICMS10" + item + ".vBCFCPST") + "|" + x.get("ICMS10" + item + ".pFCPST") + "|" + x.get("ICMS10" + item + ".vFCPST") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS20") != null) {
                o.append("N04|" + x.get("ICMS20" + item + ".orig") + "|" + x.get("ICMS20" + item + ".CST") + "|" + x.get("ICMS20" + item + ".modBC") + "|" + x.get("ICMS20" + item + ".pRedBC") + "|" + x.get("ICMS20" + item + ".vBC") + "|" + x.get("ICMS20" + item + ".pICMS") + "|" + x.get("ICMS20" + item + ".vICMS") + "|" + x.get("ICMS20" + item + ".vBCFCP") + "|" + x.get("ICMS20" + item + ".pFCP") + "|" + x.get("ICMS20" + item + ".vFCP") + "|" + x.get("ICMS20" + item + ".vICMSDeson") + "|" + x.get("ICMS20" + item + ".motDesICMS") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS30") != null) {
                o.append("N05|" + x.get("ICMS30" + item + ".orig") + "|" + x.get("ICMS30" + item + ".CST") + "|" + x.get("ICMS30" + item + ".modBCST") + "|" + x.get("ICMS30" + item + ".pMVAST") + "|" + x.get("ICMS30" + item + ".pRedBCST") + "|" + x.get("ICMS30" + item + ".vBCST") + "|" + x.get("ICMS30" + item + ".pICMSST") + "|" + x.get("ICMS30" + item + ".vICMSST") + "|" + x.get("ICMS30" + item + ".vBCFCPST") + "|" + x.get("ICMS30" + item + ".pFCPST") + "|" + x.get("ICMS20" + item + ".vFCPST") + "|" + x.get("ICMS30" + item + ".vICMSDeson") + "|" + x.get("ICMS30" + item + ".motDesICMS") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS40") != null || x.get("ICMS" + item + ".ICMS41") != null || x.get("ICMS" + item + ".ICMS50") != null) {
                String tagPai = "";
                if (x.get("ICMS" + item + ".ICMS40") != null) {
                    tagPai = "ICMS40" + item;
                } else if (x.get("ICMS" + item + ".ICMS41") != null) {
                    tagPai = "ICMS41" + item;
                } else if (x.get("ICMS" + item + ".ICMS50") != null) {
                    tagPai = "ICMS50" + item;
                }
                o.append("N06|" + x.get(tagPai + ".orig") + "|" + x.get(tagPai + ".CST") + "|" + x.get(tagPai + ".vICMSDeson") + "|" + x.get(tagPai + ".motDesICMS") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS51") != null) {
                o.append("N07|" + x.get("ICMS51" + item + ".orig") + "|" + x.get("ICMS51" + item + ".CST") + "|" + x.get("ICMS51" + item + ".modBC") + "|" + x.get("ICMS51" + item + ".pRedBC") + "|" + x.get("ICMS51" + item + ".VBC") + "|" + x.get("ICMS51" + item + ".pICMS") + "|" + x.get("ICMS51" + item + ".vICMSOp") + "|" + x.get("ICMS51" + item + ".pDif") + "|" + x.get("ICMS51" + item + ".vICMSDif") + "|" + x.get("ICMS51" + item + ".vICMS") + "|" + x.get("ICMS51" + item + ".vBCFCP") + "|" + x.get("ICMS51" + item + ".pFCP") + "|" + x.get("ICMS51" + item + ".vFCP") + "|\n");
            } else if (x.get("ICMS" + item + ".ICMS60") != null) {
                o.append("N08|" + x.get("ICMS60" + item + ".orig") + "|" + x.get("ICMS60" + item + ".CST") + "|" + x.get("ICMS60" + item + ".vBCSTRet") + "|" + x.get("ICMS60" + item + ".pST") + "|" + x.get("ICMS60" + item + ".vICMSSTRet") + "|" + x.get("ICMS60" + item + ".vBCFCPSTRet") + "|" + x.get("ICMS60" + item + ".pFCPSTRet") + "|" + x.get("ICMS60" + item + ".vFCPSTRet") + "|\n");

            } else if (x.get("ICMS" + item + ".ICMS70") != null) {
                o.append("N09|" + x.get("ICMS70" + item + ".orig") + "|" + x.get("ICMS70" + item + ".CST") + "|" + x.get("ICMS70" + item + ".modBC") + "|" + x.get("ICMS70" + item + ".pRedBC") + "|" + x.get("ICMS70" + item + ".vBC") + "|" + x.get("ICMS70" + item + ".pICMS") + "|" + x.get("ICMS70" + item + ".vICMS") + "|" + x.get("ICMS70" + item + ".vBCFCP") + "|" + x.get("ICMS70" + item + ".pFCP") + "|" + x.get("ICMS70" + item + ".vFCP") + x.get("ICMS70" + item + ".modBCST") + "|" + x.get("ICMS70" + item + ".pMVAST") + "|" + x.get("ICMS70" + item + ".pRedBCST") + "|" + x.get("ICMS70" + item + ".vBCST") + "|" + x.get("ICMS70" + item + ".pICMSST") + "|" + x.get("ICMS70" + item + ".vICMSST") + "|" + x.get("ICMS70" + item + ".vBCFCPST") + "|" + x.get("ICMS70" + item + ".pFCPST") + "|" + x.get("ICMS70" + item + ".vFCPST") + "|" + x.get("ICMS70" + item + ".vICMSDeson") + "|" + x.get("ICMS70" + item + ".motDesICMS") + "|\n");

            } else if (x.get("ICMS" + item + ".ICMS90") != null) {
                o.append("N10|" + x.get("ICMS90" + item + ".orig") + "|" + x.get("ICMS90" + item + ".CST") + "|" + x.get("ICMS90" + item + ".modBC") + "|" + x.get("ICMS90" + item + ".pRedBC") + "|" + x.get("ICMS90" + item + ".vBC") + "|" + x.get("ICMS90" + item + ".pICMS") + "|" + x.get("ICMS90" + item + ".vICMS") + "|" + x.get("ICMS90" + item + ".vBCFCP") + "|" + x.get("ICMS90" + item + ".pFCP") + "|" + x.get("ICMS90" + item + ".vFCP") + "|" + x.get("ICMS90" + item + ".modBCST") + "|" + x.get("ICMS90" + item + ".pMVAST") + "|" + x.get("ICMS90" + item + ".pRedBCST") + "|" + x.get("ICMS90" + item + ".vBCST") + "|" + x.get("ICMS90" + item + ".pICMSST") + "|" + x.get("ICMS90" + item + ".vICMSST") + "|" + x.get("ICMS90" + item + ".vBCFCPST") + "|" + x.get("ICMS90" + item + ".pFCPST") + "|" + x.get("ICMS90" + item + ".vFCPST") + "|" + x.get("ICMS90" + item + ".vICMSDeson") + "|" + x.get("ICMS90" + item + ".motDesICMS") + "|\n");
            }
            //ITENS IGNORADOS
            //N10a|orig|CST|modBC|pRedBC|vBC|pICMS|vICMS|modBCST|pMVAST|pRedBCST|vBCST|pICMSST|vICMSST|pBCOp|UFST|
            //N10b|orig|CST|vBCSTRet|vICMSSTRet|vBCSTDest|vICMSSTDest|
            //N10c|orig|CSOSN|pCredSN|vCredICMSSN|
            //N10d|orig|CSOSN|
            //N10e|orig|CSOSN|modBCST|pMVAST|pRedBCST|vBCST|pICMSST|vICMSST|pCredSN|vCredICMSSN|
            //N10f|orig|CSOSN|modBCST|pMVAST|pRedBCST|vBCST|pICMSST|vICMSST|
            //N10g|orig|CSOSN|vBCSTRet|vICMSSTRet|
            //N10h|orig|CSOSN|modBC|vBC|pRedBC|pICMS|vICMS|modBCST|pMVAST|pRedBCST|vBCST|pICMSST|vICMSST|pCredSN|vCredICMSSN|
            //        ----------------------------------------------------
            if (x.get("imposto" + item + ".IPI") != null) {
                o.append("O|" + x.get("IPI" + item + ".CNPJProd") + "|" + x.get("IPI" + item + ".cSelo") + "|" + x.get("IPI" + item + ".qSelo") + "|" + x.get("IPI" + item + ".cEnq") + "|\n");

                if ((x.get("IPI" + item + ".IPITrib") != null)) {
                    o.append("O07|" + x.get("IPITrib" + item + ".CST") + "|" + x.get("IPITrib" + item + ".vIPI") + "|\n");
                    if (x.get("IPITrib" + item + ".pIPI") != null) {
                        o.append("O10|" + x.get("IPITrib" + item + ".vBC") + "|" + x.get("IPITrib" + item + ".pIPI") + "|\n");
                    } else {
                        o.append("O11|" + x.get("IPITrib" + item + ".qUnid") + "|" + x.get("IPITrib" + item + ".vUnid") + "|\n");
                    }
                } else if ((x.get("IPI" + item + ".IPINT") != null)) {
                    o.append("O08|" + x.get("IPINT" + item + ".CST") + "|\n");
                }
            }

//        ----------------------------------------------------
//            IMPOSTO DE IMPORTAÃ‡Ã‚O
            if (x.get("imposto" + item + ".II") != null) {
//                System.out.println("II" + item + ".VBC" + x.get("II" + item + ".VBC"));
                o.append("P|" + x.get("II" + item + ".vBC") + "|" + x.get("II" + item + ".vDespAdu") + "|" + x.get("II" + item + ".vII") + "|" + x.get("II" + item + ".vIOF") + "|\n");
            }
//        ----------------------------------------------------
            if (x.get("imposto" + item + ".PIS") != null) {
                o.append("Q|\n");
                if (x.get("PISAliq" + item + ".CST") != null) {
                    o.append("Q02|" + x.get("PISAliq" + item + ".CST") + "|" + x.get("PISAliq" + item + ".vBC") + "|" + x.get("PISAliq" + item + ".pPIS") + "|" + x.get("PISAliq" + item + ".vPIS") + "|\n");
                } else if (x.get("PISQtde" + item + ".CST") != null) {
                    o.append("Q03|" + x.get("PISQtde" + item + ".CST") + "|" + x.get("PISQtde" + item + ".qBCProd") + "|" + x.get("PISQtde" + item + ".vAliqProd") + x.get("PISQtde" + item + ".vPIS") + "|\n");
                } else if (x.get("PISNT" + item + ".CST") != null) {
                    o.append("Q04|" + x.get("PISNT" + item + ".CST") + "|\n");
                } else if (x.get("PISOutr" + item + ".CST") != null) {
                    o.append("Q05|" + x.get("PISOutr" + item + ".CST") + "|" + x.get("PISOutr" + item + ".vPIS") + "|\n");
                }
            }
//        ----------------------------------------------------
            if (x.get("PIS" + item + ".PISST") != null) {
                o.append("R|" + x.get("PISST" + item + ".vPIS") + "|\n");

                if (x.get("PISST" + item + ".pPIS") != null) {
                    o.append("R02|" + x.get("PISST" + item + ".vBC") + "|" + x.get("PISST" + item + ".pPIS") + "|\n");
                } else {
                    o.append("R04|" + x.get("PISST" + item + ".qBCProd") + "|" + x.get("PISST" + item + ".vAliqProd") + x.get("PISST" + item + ".vPIS") + "|\n");
                }
            }
//        ----------------------------------------------------
            if (x.get("imposto" + item + ".COFINS") != null) {
                o.append("S|\n");
                if (x.get("COFINSAliq" + item + ".CST") != null) {
                    o.append("S02|" + x.get("COFINSAliq" + item + ".CST") + "|" + x.get("COFINSAliq" + item + ".vBC") + "|" + x.get("COFINSAliq" + item + ".pCOFINS") + "|" + x.get("COFINSAliq" + item + ".vCOFINS") + "|\n");
                } else if (x.get("COFINSQtde" + item + ".CST") != null) {
                    o.append("S03|" + x.get("COFINSQtde" + item + ".CST") + "|" + x.get("COFINSQtde" + item + ".qBCProd") + "|" + x.get("COFINSQtde" + item + ".vAliqProd") + "|" + x.get("COFINSQtde" + item + ".vCOFINS") + "|\n");
                } else if (x.get("COFINSNT" + item + ".CST") != null) {
                    o.append("S04|" + x.get("COFINSNT" + item + ".CST") + "|\n");
                } else if (x.get("COFINSOutr" + item + ".CST") != null) {
                    o.append("S05|" + x.get("COFINSOutr" + item + ".CST") + "|" + x.get("COFINSOutr" + item + ".vCOFINS") + "|\n");
                    if (x.get("COFINSOutr" + item + ".pCOFINS") != null) {
                        o.append("S07|" + x.get("COFINSOutr" + item + ".vBC") + "|" + x.get("COFINSOutr" + item + ".pCOFINS") + "|\n");
                    } else {
                        o.append("S06|" + x.get("COFINSOutr" + item + ".qBCProd") + "|" + x.get("COFINSOutr" + item + ".vAliqProd") + "|\n");
                    }
                }
//        ----------------------------------------------------
                if (x.get("COFINS" + item + ".COFINSST") != null) {
                    o.append("T|" + x.get("COFINSST" + item + ".vCOFINS") + "|\n");
                    if (x.get("COFINSST" + item + ".pCOFINS") != null) {
                        o.append("T02|" + x.get("COFINSST" + item + ".vBC") + "|" + x.get("COFINSST" + item + ".pCOFINS") + "|\n");
                    } else {
                        o.append("T04|" + x.get("COFINSST" + item + ".qBCProd") + "|" + x.get("COFINSST" + item + ".vAliqProd") + "|\n");
                    }
                }
            }
//        ----------------------------------------------------
            //            ITEM IGNORADO
            //U|vBC|vAliq|vISSQN|cMunFG|cListServ|vDeducao|vOutro|vDescIncond|vDescCond|vISSRet|indISS|cServico|cM
            //un|cPais|nProcesso|indIncentivo|

//        ----------------------------------------------------
            item++;

        }
//        FIM DE ITENS
//        ----------------------------------------------------
        o.append("W|\n");
        if (x.get("total.ICMSTot") != null) {
            o.append("W02|" + x.get("ICMSTot.vBC") + "|" + x.get("ICMSTot.vICMS") + "|" + x.get("ICMSTot.vICMSDeson") + "|" + x.get("ICMSTot.vFCP") + "|" + x.get("ICMSTot.vBCST") + "|" + x.get("ICMSTot.vST") + "|" + x.get("ICMSTot.vFCPST") + "|" + x.get("ICMSTot.vFCPSTRet") + "|" + x.get("ICMSTot.vProd") + "|" + x.get("ICMSTot.vFrete") + "|" + x.get("ICMSTot.vSeg") + "|" + x.get("ICMSTot.vDesc") + "|" + x.get("ICMSTot.vII") + "|" + x.get("ICMSTot.vIPI") + "|" + x.get("ICMSTot.vIPIDevol") + "|" + x.get("ICMSTot.vPIS") + "|" + x.get("ICMSTot.vCOFINS") + "|" + x.get("ICMSTot.vOutro") + "|" + x.get("ICMSTot.vNF") + "|" + x.get("ICMSTot.vTotTrib") + "|" + x.get("ICMSTot.vFCPUFDest") + "|" + x.get("ICMSTot.vICMSUFDest") + "|" + x.get("ICMSTot.vICMSUFRemet") + "|\n");

        }
        if (x.get("total.ISSQNTot") != null) {
            o.append("W17|" + x.get("ISSQNTot.vServ") + "|" + x.get("ISSQNTot.vBC") + "|" + x.get("ISSQNTot.vISS") + "|" + x.get("ISSQNTot.vPIS") + "|" + x.get("ISSQNTot.vCOFINS") + "|" + x.get("ISSQNTot.dCompet") + "|" + x.get("ISSQNTot.vDeducao") + "|" + x.get("ISSQNTot.vOutro") + "|" + x.get("ISSQNTot.vDescIncond") + "|" + x.get("ISSQNTot.vDescCond") + "|" + x.get("ISSQNTot.vISSRet") + "|" + x.get("ISSQNTot.cRegTrib") + "|\n");
        }
        if (x.get("total.retTrib ") != null) {
            o.append("W23|" + x.get("retTrib.vRetPIS") + "|" + x.get("retTrib.vRetCOFINS") + "|" + x.get("retTrib.vRetCSLL") + "|" + x.get("retTrib.vBCIRRF") + "|" + x.get("retTrib.vIRRF") + "|" + x.get("retTrib.vBCRetPrev") + "|" + x.get("retTrib.vRetPrev") + "|\n");
        }
//        ----------------------------------------------------
        o.append("X|" + x.get("transp.modFrete") + "|\n");
        if ((x.get("transporta.xNome") != null) || (x.get("transporta.IE") != null) || (x.get("transporta.xEnder") != null) || (x.get("transporta.UF") != null) || (x.get("transporta.xMun") != null)) {
            o.append("X03|" + x.get("transporta.xNome") + "|" + x.get("transporta.IE") + "|" + x.get("transporta.xEnder") + "|" + x.get("transporta.UF") + "|" + x.get("transporta.xMun") + "|\n");
        }
        if (x.get("transporta.CNPJ") != null) {
            o.append("X04|" + x.get("transporta.CNPJ") + "|\n");
        }
        if (x.get("transporta.CPF") != null) {
            o.append("X05|" + x.get("transporta.CPF") + "|\n");
        }
        if (x.get("transp.retTransp") != null) {
            o.append("X11|" + x.get("veicTransp.vServ") + "|" + x.get("veicTransp.vBCRet") + "|" + x.get("veicTransp.pICMSRet") + "|" + x.get("veicTransp.vICMSRet") + "|" + x.get("veicTransp.CFOP") + "|" + x.get("veicTransp.cMunFG") + "|\n");
        }
        if (x.get("transp.veicTransp") != null) {
            o.append("X18|" + x.get("veicTransp.Placa") + "|" + x.get("veicTransp.UF") + "|" + x.get("veicTransp.RNTC") + "|\n");
        }
        //        IGNORADo
        //        [0a 5] {
        //X22 | Placa | UF | RNTC |
        //        }
        //    }
        //        [0 a 5000] {
//        X26|qVol|esp|marca|nVol|pesoL|pesoB|

        //[0 a 5000] {
        if (x.get("transp.vol" + volume) != null) {
            o.append("X26|" + x.get("vol" + volume + ".qVol") + "|" + x.get("vol" + volume + ".esp") + "|" + x.get("vol" + volume + ".marca") + "|" + x.get("vol" + volume + ".nVol") + "|" + x.get("vol" + volume + ".pesoL") + "|" + x.get("vol" + volume + ".pesoB") + "|\n");

            volume++;
        }
        //X33|nLacre|
        //}
        //}
//        ----------------------------------------------------
        o.append("Y|\n");
        if (x.get("cobr.fat") != null) {
            o.append("Y02|" + x.get("fat.nFat") + "|" + x.get("fat.vOrig") + "|" + x.get("fat.vDesc") + "|" + x.get("fat.vLiq") + "|\n");
        }
        for (
                int i = 1;
                i <= 120; i++) {
            if (x.get("cobr" + i + ".dup") != null) {
                o.append("Y07|" + x.get("dup" + i + ".nDup") + "|" + x.get("dup" + i + ".dVenc") + "|" + x.get("dup" + i + ".vDup") + "|\n");
            } else {
                break;
            }
        }

        if (x.get("pag.detPag") != null) {
            for (int i = 1; i <= 120; i++) {
                if (x.get("pag.detPag" + i + ".tPag") != null) {
                    o.append("YA|" + x.get("pag.detPag" + i + ".tPag") + "|" + x.get("pag.detPag" + i + ".vPag") + "|" + x.get("detPag" + i + ".tpIntegra") + "|" + x.get("pag.detPag" + i + ".CNPJ") + "|" + x.get("pag.detPag" + i + ".tBand") + "|" + x.get("pag.detPag" + i + ".cAut") + "|" + x.get("pag.detPag" + i + ".vTroco") + "|\n");
                } else {
                    break;
                }
            }
        }

//        [0 ou 1] {
//Z|InfAdFisco|InfCpl|
        if ((x.get("infAdic.InfAdFisco") != null) || (x.get("infAdic.infCpl") != null)) {
            o.append("Z|" + x.get("infAdic.infAdFisco") + "|" + x.get("infAdic.infCpl") + "|\n");
        }

//[0 a 10] {
//Z04|xCampo|xTexto|
        for (
                int i = 1;
                i <= 10; i++) {
            if (x.get("obsCont" + i + ".xCampo") != null || x.get("obsCont" + i + ".xTexto") != null) {
                o.append("Z04|" + x.get("obsCont" + i + ".xCampo") + "|" + x.get("obsCont" + i + ".xTexto") + "|\n");
            } else {
                break;
            }
        }
//}
//[0 a 10] {w
//Z07|xCampo|xTexto|
        for (
                int i = 1;
                i <= 10; i++) {
            if (x.get("obsFisco" + i + ".xCampo") != null || x.get("obsFisco" + i + ".xTexto") != null) {
                o.append("Z07|" + x.get("obsFisco" + i + ".xCampo") + "|" + x.get("obsFisco" + i + ".xTexto") + "|\n");
            } else {
                break;
            }
        }
//}
//[0 a 100] {
//Z10|nProc|indProc|
        for (
                int i = 1;
                i <= 10; i++) {
            if (x.get("procRef" + i + ".nProc") != null || x.get("procRef" + i + ".indProc") != null) {
                o.append("Z10|" + x.get("procRef" + i + ".nProc") + "|" + x.get("procRef" + i + ".indProc") + "|\n");
            } else {
                break;
            }
        }
//}
//}
//[0 ou 1] {
//ZA|UFSaidaPais|xLocExporta|xLocDespacho|
        if (x.get("exporta.UFSaidaPais") != null) {
            o.append("ZA|" + x.get("exporta.UFSaidaPais") + "|" + x.get("exporta.xLocExporta") + "|" + x.get("exporta.xLocDespacho") + "|\n");
        }
//}
//[0 ou 1] {
//ZB|xNEmp|xPed|xCont|
        if (x.get("compra.UFSaidaPais") != null) {
            o.append("ZB|" + x.get("compra.xNEmp") + "|" + x.get("compra.xPed") + "|" + x.get("compra.xCont") + "|\n");
        }
//}
//[0 ou 1] {
//ZC01|safra|ref|qTotMes|qTotAnt|qTotGer|vFor|vTotDed|vLiqFor|
//        if (x.get("compra.UFSaidaPais") != null) {
//            o.append("ZC01|" + x.get("compra.xNEmp") + "|" + x.get("compra.xPed") + "|" + x.get("compra.xCont") + "|\n");
//        }
//[1 a 31] {
//ZC04|dia|qtde|
//}
//[0 a 10] {
//ZC10|xDed|vDed|
//}
//}
        x = null;
        return o;
    }

    /**
     * Cria arquivo txt com nome e conteudo recebidos nos parametros
     */
    public static void criaTxt(String arq, String conteudo) {
        FileWriter arquivo;

        try {
            arquivo = new FileWriter(new File(arq));
            arquivo.write(conteudo);
            arquivo.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Move arquivo
     */
    public static void moveArq(String arq, String diretorio) {
        // arquivo a ser movido
        File arquivo = new File(arq);
        // diretorio de destino
        File dir = new File(diretorio);
        // move o arquivo para o novo diretorio
        boolean ok = arquivo.renameTo(new File(dir, arquivo.getName()));

    }

    /**
     * Executavel
     */
    public static void main(String[] args) {
        String dir = "C:\\Temp\\";
        if (args.length > 0) {
            dir = args[0];
        }

        Map<String, String> x;
        File file = new File(dir + "recebimento/xml/processar");
        File arquivos[] = file.listFiles();

        for (File arq : arquivos) {
            if (arq.toString().contains(".xml")) {
                list = new HashMap<String, String>();

                try {
                    Main lexml = new Main();

                    x = lexml.lerarq(arq.toString());
                    StringBuffer o = null;
                    if (versao.equals("4.00")) {
                        o = geraDoc400((HashMap<String, String>) x);
                    } else if (versao.equals("3.10")) {
                        o = geraDoc310((HashMap<String, String>) x);
                    }
                    x.clear();

                    criaTxt(dir + "recebimento/txt/processar/" + arq.getName().replace("xml", "txt"), Pattern.compile("null").matcher(o).replaceAll(" "));

                    moveArq(arq.toString(), dir + "recebimento/xml/processado");
//                    System.out.println(Pattern.compile("null").matcher(o).replaceAll(" "));
                } catch (Exception e) {
                } finally {
                    x = new HashMap<String, String>();

                }
            }
        }
    }
}
