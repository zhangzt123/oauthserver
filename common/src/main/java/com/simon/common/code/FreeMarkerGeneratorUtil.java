package com.simon.common.code;

import com.simon.common.exception.BusinessException;
import com.simon.common.utils.DbUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 代码生成器工具类
 * @author simon
 * @date 2018-08-07
 **/
@Slf4j
public class FreeMarkerGeneratorUtil {

    /**
     * 仅生成dao层
     * @param driver
     * @param url
     * @param user
     * @param pwd
     * @param tableName
     * @param modelName
     * @param basePackage
     */
    public static void generatorMvcCode(String driver, String url, String user, String pwd, String tableName,
                                        String modelName, String basePackage) {

        Connection con = null;
        //注册驱动
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url, user, pwd);
        } catch (Exception e) {
            log.error("获取数据连接失败，{}", e.getMessage());
            return;
        }

        //获取模板路径
        String templatePath = CodeGenerator.TEMPLATE_FILE_PATH;
        //log.info("当前模板路径为：{}", templatePath);

        try {

            String entityDir = null;
            //根据实体包名创建目录
            File projectPathFile = new File(CodeGenerator.PROJECT_PATH);
            if (!projectPathFile.isDirectory()) {
                throw new IOException("Path " + CodeGenerator.PROJECT_PATH + " is not directory!");
            }
            File[] ls = projectPathFile.listFiles();
            for (File f: ls) {
                String currModule = f.toString();
                boolean matches = currModule.matches("(.*?pojo.*?)|(.*?domain.*?)|(.*?entity.*?)");
                if (f.isDirectory()&&matches){
                    entityDir = f.toString()+ CodeGenerator.JAVA_PATH + "/" + basePackage.replace(".", "/");
                    break;
                }
            }
            if (StringUtils.isBlank(entityDir)){
                entityDir = CodeGenerator.PROJECT_PATH + CodeGenerator.JAVA_PATH + "/" + basePackage.replace(".", "/");
            }
            File entityDirFile = new File(entityDir);
            if (!entityDirFile.exists()) {
                entityDirFile.mkdirs();
                log.info("创建目录：{} 成功！ ",entityDir);
            }
            EntityDataModel entityModel = DbUtil.getEntityModel(con, tableName, CodeGenerator.BASE_PACKAGE, modelName);
            //生成每个表实体
            generateCode(entityModel, templatePath, "entity.ftl", entityDir);
        } catch (Exception e) {
            log.error("代码生成出错 {}", e.getMessage());
        }

    }

    /**
     *
     * @param dataModel
     * @param templatePath
     * @param templateName
     * @param outDir
     * @throws IOException
     * @throws TemplateException
     */
    private static void generateCode(EntityDataModel dataModel, String templatePath, String templateName, String outDir)
            throws IOException, TemplateException {

        //String file = outDir +"/"+ dataModel.getEntityName() + dataModel.getFileSuffix();
        String file;
        if(templateName.contains("entity")){
            file = outDir +"/"+ dataModel.getEntityName() + dataModel.getFileSuffix();
        }else if(templateName.contains("provider")){
            file = outDir + "/" + dataModel.getEntityName() + "Provider" + dataModel.getFileSuffix();
        }else{
            throw new BusinessException("不支持的模板");
        }
        File targetFile = new File(file);
        if (targetFile.exists()){
            Files.delete(targetFile.toPath());
        }
        //获取模板对象
        Configuration conf = new Configuration(freemarker.template.Configuration.VERSION_2_3_23);
        File temp = new File(templatePath);
        conf.setDirectoryForTemplateLoading(temp);
        Template template = conf.getTemplate(templateName);
        Writer writer = new FileWriter(file);
        //填充数据模型
        template.process(dataModel, writer);
        writer.close();
        log.info("代码生成成功，文件位置：{}",file);
    }
}