package com.github.ngodat0103.usersvc.dto.mapper;

import com.github.ngodat0103.usersvc.dto.AccountDto;
import com.github.ngodat0103.usersvc.dto.topic.TopicRegisteredUser;
import com.github.ngodat0103.usersvc.persistence.document.Account;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
  AccountDto toDto(Account account);

  Account toDocument(AccountDto dto);

  TopicRegisteredUser toTopicRegisteredUser(Account account);

  //  default TopicRegisteredUser toTopicRegisteredUser(
  //      Account account,
  //      TopicRegisteredUser.Action action,
  //      Map<String, Object> additionalProperties) {
  //
  //    AccountDto accountDto = toDto(account);
  //    additionalProperties.put("accountDto", accountDto);
  //    return TopicRegisteredUser.builder()
  //        .createdDate(LocalDateTime.now().toInstant(ZoneOffset.UTC))
  //        .action(action)
  //        .additionalProperties(additionalProperties)
  //        .build();
  //  }
}
