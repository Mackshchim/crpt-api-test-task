package com.example.crptapi.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.util.Date;
import java.util.List;

@Slf4j
public class CrptApi {

    private final ObjectMapper objectMapper;
    private long requestsLimitInterval;
    private long requestsLimit;
    private String encoding;

    private long requestsCount;
    private long countUntil;

    public CrptApi(ObjectMapper objectMapper, long requestsLimitInterval, long requestsLimit, String encoding) {
        if (requestsLimit <= 0 || requestsLimit <= 0) {
            throw new RuntimeException("Limits must be real numbers");
        }
        this.objectMapper = objectMapper;
        this.requestsLimitInterval = requestsLimitInterval;
        this.requestsLimit = requestsLimit;
        this.encoding = encoding;

        requestsCount = 0;
        countUntil = System.currentTimeMillis() + requestsLimitInterval;
    }

    /* В тз указан пример тела запроса, реализация соотвествует этому примеру
     * В документации в примере запроса на данный URL
     * тело представленное вашим тз является лишь частью тела из документации.
     * По этой причине я сделал допущение (не включая подпись в реализацию данного метода).
     * ССЫЛКА НА ТЗ
     * https://docs.yandex.ru/docs/view?url=ya-disk-public%3A%2F%2FM%2B4ebMFfqCeFaKXdSmxWB9txhzSMzHPgq4wNu%2BmE5topgmfbZFWakXpCZvruMc8Uq%2FJ6bpmRyOJonT3VoXnDag%3D%3D&name=%D0%A2%D0%B5%D1%81%D1%82%D0%BE%D0%B2%D0%BE%D0%B5%20%D0%B7%D0%B0%D0%B4%D0%B0%D0%BD%D0%B8%D0%B5.docx&nosw=1
     * ССЫЛКА НА ДОКУМЕНТАЦИЮ
     * https://pub.aoasp.ru/2_%D0%B0%D1%81%D0%BF.%D0%BC%D0%B0%D1%80%D0%BA%D0%B8%D1%80%D0%BE%D0%B2%D0%BA%D0%B0:7_api:1_api_%D0%B3%D0%B8%D1%81_%D0%BC%D1%82_v18.1:2_%D0%BC%D0%B5%D1%82%D0%BE%D0%B4%D1%8B:2.2_%D0%BC%D0%B5%D1%82%D0%BE%D0%B4%D1%8B_%D0%B4%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%BE%D0%B2:2.2.1_%D0%B5%D0%B4%D0%B8%D0%BD%D1%8B%D0%B9_%D0%BC%D0%B5%D1%82%D0%BE%D0%B4_%D1%81%D0%BE%D0%B7%D0%B4%D0%B0%D0%BD%D0%B8%D1%8F_%D0%B4%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D0%BE%D0%B2:%D0%BD%D0%B0%D1%87%D0%B0%D0%BB%D0%BE
     */
    public String doCreateRequest(String uri, Document document, String sign) {
        if (!executionIsAllowed()) return "";

        try {
            HttpPost postRequest = new HttpPost(uri);
            String documentJson = objectMapper.writeValueAsString(document);
            HttpEntity entity = new StringEntity(documentJson);
            postRequest.setEntity(entity);
            try (
                    CloseableHttpClient client = HttpClients.createDefault();
                    CloseableHttpResponse response = client.execute(new HttpPost());
                    InputStream input = new BufferedInputStream(response.getEntity().getContent());
            ) {
                byte[] responseBytes = input.readAllBytes();
                String responseContent = new String(responseBytes,encoding);
                return responseContent;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return "";
        }

    }

    private synchronized boolean executionIsAllowed() {
        if (requestsCount <= requestsLimit) {
            requestsCount++;
            return true;
        } else if (countUntil <= System.currentTimeMillis()) {
            countUntil = System.currentTimeMillis() + requestsLimitInterval;
            requestsCount = 1;
            return true;
        }
        return false;
    }

}

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
class Document {

    private Description description;
    @JsonProperty("doc_id")
    private long id;
    @JsonProperty("doc_status")
    private Status status;
    @JsonProperty("doc_type")
    private DocType type;
    private boolean importRequest;
    @JsonProperty("owner_inn")
    private String ownerInn;
    @JsonProperty("producer_inn")
    private String producerInn;
    @JsonProperty("production_date")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="GMT")
    private Date productionDate;
    @JsonProperty("production_type")
    private ProductionType productionType;
    private List<Product> products;
    @JsonProperty("reg_date")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="GMT")
    private Date registrationDate;
    @JsonProperty("reg_number")
    private long registrationNumber;

}

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
class Description {
    @JsonProperty("participantInn")
    private String participantInn;
}

enum Status {

}

enum DocType {
    LP_INTRODUCE_GOODS
}

enum ProductionType {

}

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
class Product {

    @JsonProperty("certificate_document")
    private String certificateDocument;
    @JsonProperty("certificate_document_date")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="GMT")
    private Date certificateDocumentDate;
    @JsonProperty("certificate_document_number")
    private String certificateDocumentNumber;
    @JsonProperty("owner_inn")
    private String ownerInn;
    @JsonProperty("producer_inn")
    private String producerInn;
    @JsonProperty("production_date")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd", timezone="GMT")
    private Date productionDate;
    @JsonProperty("tnved_code")
    private String tnvedCode;
    @JsonProperty("uit_code")
    private String uitCode;
    @JsonProperty("uitu_code")
    private String uituCode;

}